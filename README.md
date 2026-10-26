# ova

stateful arrays for clojure

[![Publish documentation](https://github.com/zcaudate/ova/actions/workflows/docs.yml/badge.svg)](https://github.com/zcaudate/ova/actions/workflows/docs.yml)

## DEPRECATION NOTICE

[ova](https://github.com/zcaudate/ova) has been merged into [hara](https://github.com/zcaudate/hara). Please see [updated docs](http://docs.caudate.me/hara/hara-concurrent-ova.html) for updated version.

### Installation

Add to `project.clj`

    [xyz.zcaudate/ova "1.0.1"]

Core operations are in `ova.core`; predicate selection and mutation are in
`ova.select`.

### Documentation

See main site at:

https://zcaudate.github.io/ova/

Documentation uses `code.doc`, with source pages in `src-documentation/`
and site configuration in `config/publish.edn`.

```sh
lein docs-check  # validate pages and execute their examples
lein docs        # check, initialise the theme, and build HTML
```

Open `doc/index.html` for the guide, API reference, and scoreboard example.
The first build creates the theme in `template/ova/`; subsequent builds
reuse it so local customisations are preserved. Generated HTML is ignored
by Git. Documentation tooling and source paths are confined to the dev profile.

The `Publish documentation` GitHub Actions workflow runs tests and builds the
site with Java 27. Pushes to `master` publish the generated `doc/` directory
to GitHub Pages; pull requests build and check the site without deploying it.
You can also run the workflow manually from the Actions tab.

In repository **Settings → Pages → Build and deployment**, set **Source** to
**GitHub Actions**. The workflow uses GitHub's automatic token and does not
require a personal access token.

### Blog

See [You took 3 months to write a mutable array for clojure?](http://z.caudate.me/you-took-3-months-to-write-a-mutable-array/) for motivation and write up.


## License
Copyright © 2013 Chris Zheng

Distributed under the MIT License
