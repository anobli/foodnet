#!/bin/sh

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output google_ads.properties .github/workflows/google_ads.properties.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output app/google-services.json .github/workflows/google-services.json.gpg
