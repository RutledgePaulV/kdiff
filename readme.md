### What

A tool for diffing kubernetes resources that may be scattered across many yaml files / directories.

### Why

Sometimes you come across a set of kubernetes manifests and want to know how they differ from other sets of manifests.

### How

It's a Clojure program that gathers / parses / indexes kubernetes resources across a set of files and then runs a diff
algorithm against the indexed sets of resources and then pretty prints just the differences (as Clojure data). The
release distributions are native binaries compiled with GraalVM so there are no required dependencies for users.

