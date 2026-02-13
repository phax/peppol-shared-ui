# peppol-shared-ui

Shared UI components for Peppol related websites like peppol.helger.com
This includes primarily UI and web application related components.

As the UI framework used is proprietory (ph-oton) and the users are limited and the version is pre 1.0 I decided to not to spend too much effort in documenting all the detailed changes. Expect this to start after 1.0.

This project consists of the following modules, in dependency order:
* `peppol-ui-types` - general UI related data types
* `peppol-ui` - general UI related help classes etc.
* `peppol-shared-api` - a set of commonly provided APIs around Peppol
* `peppol-shared-ui` - more advanced UI elements like pages etc.
* `peppol-shared-as4` - shared UI components related to AS4 message transmission
* `peppol-shared-validation` - shared UI components related to document validation

# News and Noteworthy

v0.9.12 - 2026-02-13
* Extracted API support methods into reusable components

v0.9.11 - 2026-01-23
* Updated to peppol-commons 12.3.7
* Showing the DNS NAPTR URL for Peppol also if participant was not found
* Showing a warning in the Participant Information UI if the SMP base URL differs from the per document type base URL

v0.9.10 - 2025-12-29
* Updated to peppol-commons 12.3.4
* Belgium Participant Check was improved with syntax check and multi check possibility
* Updated to eDEC Code Lists v9.5
* Renamed `IMiniCallback*` to `IFeedbackCallback*`

v0.9.9 - 2025-12-14
* Misc small improvements

v0.9.8 - 2025-12-06
* Added HR eRacun validation
* Improved error handling in SMP query APIs
* Unified API handling for shared usage
* Improvements in participant information UI
    * Showing SMP signing certificate details
    * Collapsing document type overview list be default
    * Made Certificate details UI collapsible and showing owner more clearly

v0.9.7 - 2025-11-16
* Updated to ph-commons 12.1.0
* Using JSpecify annotations

v0.9.6 - 2025-11-14
* Updated to peppol-commons 12.1.3 fixing an issue with determining document types from SMP responses

v0.9.5 - 2025-11-07
* Allowing a more fine grained rate limit configuration for the Validation API

v0.9.4 - 2025-11-07
* Allowing a more fine grained rate limit configuration for the REST API

v0.9.3 - 2025-11-04
* Removed the `https` requirement for Peppol SMP URLs in production
* Removed the possibility to query with CNAME

v0.9.2 - 2025-10-29
* Updated to peppol-commons 12.1.0
* The SML URL suffixes to manage SMPs and Participants are now editable (required for HR)

v0.9.1 - 2025-10-28
* Removed the hardcoded "Peppol " prefix before Document Type code list entries 

v0.9.0 - 2025-10-20
* Initial release
