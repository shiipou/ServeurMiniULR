#include <stdlib.h>
#include <stdio.h>

int main(){

	fprintf(stdout, "Content-type: text/html\n");
	fprintf(stdout, "Location: https://www.google.fr/search?q=");

	char t;
    unsigned int req = 0;

	while((t = getc(stdin)) != '\n' && t != EOF){
        if(req == 2)
		    fprintf(stdout, "%c", t);

	    if(t == 'q')
            req = 1;

        if(req == 1)
            if(t == '=')
                req = 2;
	}
	fprintf(stdout, "\n\n");
	return 0;
}
