:warning: *This is currently a stub, it will be expanded in the future.*

It's great that you are taking the time to contribute to this repository! :+1:

This document details some guidelines for reporting / managing issues, writing code, doing releases and such things. They aren't rules set in stone, so feel free to contribute changes!

## Reporting Issues ##

- Check if already reported
- Environment, versions (OS, Ansible, Rundeck, Plugin) etc.
- How to reproduce

## Managing Issues ##

Tell people to try a new release, if their request is addressed in it.

### Labels ###

The goal is to attach at least one label to every issue (even user-closed ones). This makes for a nice overview and some stats.

- Usually one of:
    - bug (something is broken and needs to be fixed)
    - enhancement (new or better functionality)
    - question (unclear if bug or enhancement / configuration question / general usage question)
- Or one of:
    - duplicate (a matching issue already exists, close and link to it)
    - meta (project management stuff)
- These can be added additionally:
    - stalled (no response for a while, will be closed soon)
    - nope (declined bug or enhancement)

## Code Style ##

TBD

## Releases ##

Version / tags / release names have a leading 'v', consist of major.minor.patch and use [Semantic Versioning](http://semver.org/).

GitHub releases will automatically be published via GitHub Actions. Once a `vX.X.X` tag is created or pushed, a jar will be built, and added to the release.

## Docker ##

A Docker image will be automatically built and published for tags on the master branch. 

Periodically update the `Dockerfile` with newer Ansible and Rundeck versions - see the comments there for where to find the newest versions.
