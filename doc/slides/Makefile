all: slides.pdf

slides.pdf: slides.hal
	hal slides.hal > slides.tex
	pdflatex slides.tex
	pdflatex slides.tex
	rm test.txt
