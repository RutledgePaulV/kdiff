### What

A tool for diffing kubernetes resources that may be scattered across many yaml files / directories.

### Why

Sometimes you come across a set of kubernetes manifests and want to know how they differ from other sets of manifests.

### How

It's a Clojure program that gathers / parses / indexes kubernetes resources across a set of files and then runs a diff
algorithm against the indexed sets of resources and then pretty prints just the differences (as Clojure data). The
release distributions are native binaries compiled with GraalVM so there are no required dependencies for users.

### Usage 

``` 

NAME:
 kdiff diff - Diff kubernetes resources across files / directories.

USAGE:
 kdiff diff [command options] [arguments...]

EXAMPLES:
 kdiff diff --old old.yml --new new.yml
 kdiff diff --old old/manifests --new new/manifests

OPTIONS:
   -o, --old S*                               An old (expected) manifest file or directory.
   -n, --new S*                               An new (actual) manifest file or directory.
   -iar, --ignore-added-resources F    false  Skip displaying diff for entirely new resources.
   -irr, --ignore-removed-resources F  false  Skip displaying diff for entirely removed resources.
   -?, --help

```