MIREVal – Evaluates [MIaS][] results
====================================
[![CircleCI](https://circleci.com/gh/MIR-MU/MIREVal/tree/master.svg?style=shield)][ci]

 [ci]: https://circleci.com/gh/MIR-MU/MIREVal/tree/master (CircleCI)

MIREval takes final result lists produced by the [NTCIR MIaS
Search][ntcir-mias-search] package, compares them with relevance judgements in
the [`trec_eval`][trec_eval] format, and produces the following ranked
retrieval evaluation metrics:

- Bpref at 10, 20, 50, 100, and 1000
- Mean Average Presision
- Precision at 1, 2, 3, 4, 5, 10, 15, and 20

 [mias]: https://github.com/MIR-MU/MIaS
 [ntcir-mias-search]: https://github.com/MIR-MU/ntcir-mias-search
 [trec_eval]: https://github.com/usnistgov/trec_eval

Citing MIREval
==============
Text
----
SOJKA, Petr and Martin LÍŠKA. The Art of Mathematics Retrieval. In Matthew R.
B. Hardy, Frank Wm. Tompa. *Proceedings of the 2011 ACM Symposium on Document
Engineering.* Mountain View, CA, USA: ACM, 2011. p. 57–60. ISBN
978-1-4503-0863-2. doi:[10.1145/2034691.2034703][doi].

 [doi]: http://doi.org/10.1145/2034691.2034703

BibTeX
------
``` bib
@inproceedings{doi:10.1145:2034691.2034703,
     author = "Petr Sojka and Martin L{\'\i}{\v s}ka",
      title = "{The Art of Mathematics Retrieval}",
  booktitle = "{Proceedings of the ACM Conference on Document Engineering,
  		DocEng 2011}",
  publisher = "{Association of Computing Machinery}",
    address = "{Mountain View, CA}",
       year = 2011,
      month = Sep,
       isbn = "978-1-4503-0863-2",
      pages = "57--60",
        url = {http://doi.acm.org/10.1145/2034691.2034703},
        doi = {10.1145/2034691.2034703},
   abstract = {The design and architecture of MIaS (Math Indexer and Searcher), 
	       a system for mathematics retrieval is presented, and design 
	       decisions are discussed. We argue for an approach based on 
	       Presentation MathML using a similarity of math subformulae. The 
	       system was implemented as a math-aware search engine based on the 
	       state-of-the-art system Apache Lucene. Scalability issues were 
	       checked against more than 400,000 arXiv documents with 158 
	       million mathematical formulae. Almost three billion MathML 
	       subformulae were indexed using a Solr-compatible Lucene.},
}
```
