#pluperfct-utils#

Utility library and scripts for editorial work specific to the demonstration work on canonically cited texts and analyses of Plutarch's *Pericles*, from Furman University.

## Contents

### Sample JSON

`resources/PeriklesFinal.json`: Sample JSON output from eComparatio tool (<https://github.com/ecomp-shONgit/ecomparatio>).

### Test Script

`scripts/ecomparatio.sc`: Experimental Scala script for parsing eComparatio JSON into citable texts. Run with:

~~~
   $ sbt
   $ ++2.12.3
   $ crossedJVM/console
   $ :load scripts/ecomparatio.sc
~~~

Look for output in `resources/`.

### CEX Output

The `scripts/ecomparatio.sc` script will read in the JSON output from eComparatio, which aligns two texts, and output the results as a CEX file, a plain-text serialization of data following the [CITE Architecture](http://cite-architecture.github.io).

The output will include:

1. CTS texts of both compared works, citable at the version level.
1. CTS "analytical exemplars" of both texts, citable at the word-token level.
1. A CITE Collection capturing a comprehensive, token-level `diff` operation on both texts.

At the moment, for this proof-of-concept, certain values (URN citations, citation-schemes) are hard-coded as values at the top of the `ecomparatio.sc` script.

The CITE Collection:

| Property | Type | Notes |
|----------|-------|------|
| `urn` | Cite2Urn | Makes this particular difference, itself, citable |
| `basetext` | Cite2Urn | exemplar-level URN, citing the token |
| `othertext` | Cite2Urn | exemplar-level URN, citing the token |
| `label` | String | user-displayed label for this diff; can be anything |
| `seq` | Number | The token-sequence according to the text-order of `basetext` |
| `type` | String | The type of difference. Could be made into a controlled vocabulary type |
| `distance` | The edit-distance 



