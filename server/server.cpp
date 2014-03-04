#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <assert.h>

#include <algorithm>
#include <vector>
#include <sstream>
#include <unordered_map>
#include <iostream>
#include <string>

#define PORT    3490
#define MAXMSG  512

using namespace std;

typedef struct {
  unsigned int id;
  unsigned long long hash;
  string name;
  string ip;
  unsigned short port;
} ClientDevice;

unsigned int get_client_id(const char *buffer, unordered_map<unsigned long long, unsigned int> &client_hash_to_id)
{
  unsigned long long hash;
  sscanf(buffer, "%*s (%llx) connected", &hash);
  auto find_it = client_hash_to_id.find(hash);
  if (find_it == end(client_hash_to_id))
    {
      auto client_id = client_hash_to_id.size();
      client_hash_to_id[hash] = client_id;
      return client_id;
    }
  else
    {
       return find_it->second;
    }
}

int make_socket (uint16_t port)
{
  int sock;
  struct sockaddr_in name;

  /* Create the socket. */
  sock = socket (PF_INET, SOCK_STREAM, 0);
  if (sock < 0)
    {
      perror ("socket");
      exit (EXIT_FAILURE);
    }

  /* Give the socket a name. */
  name.sin_family = AF_INET;
  name.sin_port = htons (port);
  name.sin_addr.s_addr = htonl (INADDR_ANY);
  if (bind (sock, (struct sockaddr *) &name, sizeof (name)) < 0)
    {
      perror ("bind");
      exit (EXIT_FAILURE);
    }

  return sock;
}

#define BUF_SIZE 10

int reply_to_client (int filedes, unsigned int client_id)
{
  char buf[BUF_SIZE];
  snprintf(buf, BUF_SIZE, "%u", client_id);
  string confirm_msg = "Client #" + string(buf) + " confirmed\n";
  int msg_size = confirm_msg.length() + 1;
  int nbytes = write(filedes, confirm_msg.c_str(), msg_size);
  fprintf (stderr, "Server sent message: %swith nybtes successful: %d\n", confirm_msg.c_str(), nbytes);
  return nbytes >= 0;
}

int read_from_client (int filedes, const struct sockaddr_in clientname, unordered_map<unsigned long long, unsigned int> &client_hash_to_id, vector<ClientDevice> &client_devices)
{
  char buffer[MAXMSG];
  int nbytes;

  nbytes = read (filedes, buffer, MAXMSG);
  if (nbytes < 0)
    {
      /* Read error. */
      perror ("read");
      exit (EXIT_FAILURE);
    }
  else if (nbytes == 0)
    /* End-of-file. */
    return -1;
  else
    {
      /* Data read. */
      buffer[nbytes] = '\0';
      fprintf (stderr, "Server: got message: '%s'\n", buffer);
      unsigned int client_id = get_client_id(buffer, client_hash_to_id);
      if (client_id >= client_devices.size())
        {
          assert(client_id == client_devices.size());
          string client_name;
          stringstream ss;
          ss.str(string(buffer));
          ss >> client_name;
          unsigned long long client_hash;
          sscanf(buffer, "%*s (%llx) connected", &client_hash);
          ClientDevice client_device = {client_id, client_hash, client_name, string(inet_ntoa(clientname.sin_addr)), ntohs(clientname.sin_port)};
          client_devices.push_back(client_device);
          fprintf(stderr, "Created Client Device %u %llx %s %s %hu\n", client_device.id, client_device.hash, client_device.name.c_str(), client_device.ip.c_str(), client_device.port);
        }
      return reply_to_client(filedes, client_id);
    }
}

int main (void)
{
  int sock;
  fd_set active_fd_set, read_fd_set;
  int i;
  struct sockaddr_in clientname;
  socklen_t size;

  // Keeping track of clients
  unordered_map<unsigned long long, unsigned int> client_hash_to_id;
  vector<ClientDevice> client_devices;
  unordered_map<int, struct sockaddr_in> fd_to_sockaddr;

  /* Create the socket and set it up to accept connections. */
  sock = make_socket (PORT);
  if (listen (sock, 1) < 0)
    {
      perror ("listen");
      exit (EXIT_FAILURE);
    }

  /* Initialize the set of active sockets. */
  FD_ZERO (&active_fd_set);
  FD_SET (sock, &active_fd_set);

  cerr << "Server Ready\n";
  
  while (1)
    {
      /* Block until input arrives on one or more active sockets. */
      read_fd_set = active_fd_set;
      if (select (FD_SETSIZE, &read_fd_set, NULL, NULL, NULL) < 0)
        {
          perror ("select");
          exit (EXIT_FAILURE);
        }

      /* Service all the sockets with input pending. */
      for (i = 0; i < FD_SETSIZE; ++i)
        if (FD_ISSET (i, &read_fd_set))
          {
            if (i == sock)
              {
                /* Connection request on original socket. */
                int new_fd;
                size = sizeof (clientname);
                new_fd = accept (sock,
                              (struct sockaddr *) &clientname,
                              &size);
                if (new_fd < 0)
                  {
                    perror ("accept");
                    exit (EXIT_FAILURE);
                  }
                fprintf (stderr,
                         "Server: connect from host %s, port %hu, fd %d.\n",
                         inet_ntoa (clientname.sin_addr),
                         ntohs (clientname.sin_port),
                         new_fd);
                fd_to_sockaddr[new_fd] = clientname;
                FD_SET (new_fd, &active_fd_set);
              }
            else
              {
                /* Data arriving on an already-connected socket. */
                if (read_from_client (i, fd_to_sockaddr.at(i), client_hash_to_id, client_devices) < 0)
                  {
                    close (i);
                    FD_CLR (i, &active_fd_set);
                  }
              }
          }
    }
}
