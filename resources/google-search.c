#include <stdlib.h>
#include <stdio.h>

int main(int argc, const char* argv[]){

	fprintf(stdout, "HTTP/1.1 302 OK\r\n");
	fprintf(stdout, "Content-type: text/html\r\n");
	fprintf(stdout, "Location: https://www.google.fr/search?q=%s\r\n\r\n", argv[1]);

	return 0;
}
