AndroidNotes
============

just another notes app for android. it intends to clone basic note features of google keep but syncs with evernote.
it includes following features:
* Plain text note
* Todo note
* Sync with evernote.

###Modules
it implements clean architecture and includes following modules:
* domain: java library module, contain only domain specific codes and entities.
* db: android library module, depends on domain module, implements sqlite note store.
* evernote: android library module,  depends on domain module, implements remote note store backed by evernote web service. it uses evernote sdk library for android.
* app: android application module, depends on all above 3 modules. it implements an basic android ui.
