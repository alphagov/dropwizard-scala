# Dropwizard Scala [![Build Status](https://api.travis-ci.com/alphagov/dropwizard-scala.png?token=484eMC3Pf5FGRATtNtR3)](https://magnum.travis-ci.com/alphagov/dropwizard-scala)

An opinionated wrapper and convenience classes for providing a scala-ified
dropwizard setup.

To add Dropwizard Scala to your project with sbt, add the following lines to
your `build.sbt`:

```
resolvers += "GDS Releases" at "http://alphagov.github.com/maven/releases"
libraryDependencies += "uk.gov.gds" %% "dropwizard-scala" % "0.0.9"
```

Publishing
==========

To publish a release or snapshot artefact follow these steps:

First, clone the GOV.UK Github Pages repository:

```
$ git clone git clone git@github.com:alphagov/alphagov.github.com.git
```

Then in your local Dropwizard Scala folder execute the following

```
$ sbt
> set publishMavenStyle := true
> set publishTo := Some(Resolver.file("your local repo", new java.io.File("<path>/maven/snapshots")))
> publish
```

Where <path> is the cloned folder of the `alphagov.github.com` repo.

Then switch back to the `alphagov.github.com` folder and execute the following:

```
$ git commit -am "Dropwizard Scala release/snapshot version XXX"
$ git push
```