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

