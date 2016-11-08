SealNote

[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](COPYING.md)
[![Build Status](https://travis-ci.org/vishesh/sealnote.svg?branch=master)](https://travis-ci.org/vishesh/sealnote)
[![Coverage Status](https://codecov.io/gh/vishesh/sealnote/coverage.svg?branch=master)](https://codecov.io/gh/vishesh/sealnote?branch=master)
========

SealNote is simple notes/notepad application which focuses on security and
simplicity. Your notes are password protected using 256-bit AES encryption.
Keep sensitive information always available without compromising security!

* Password protection (256-bit AES encryption)
* Color code your notes
* Password expires after configurable timeout
* Protect content from screenshots, window switcher and other non-secure
  displays
* Multi-column layout with number of columns optimized for your screen size.
* Simple and easy to use UI/UX
* No ads ever!

Advantage of encrypting notes
-----------------------------

* Keep private information such as credit cards, bank account and passwords
  in a single place
* Notes are encrypted locally so no one else can access them, unless they
  have password.
* Since encryption is done at storage level, access of notes directly from
  disk is not possible. Hence if you ever lose phone, you won't lose your
  notes to thieves.

Frequently Asked Questions (FAQ)
--------------------------------

#### Why not use XYZ app with application lock?

Application locks prevents user from opening application, but does not secure
the storage. Hence adversary can simply access the storage file directly and
read the content.

#### If someone connects phone to computer and copies/read the data file storingall the notes, can't they read everything?

They can't. No one can, regardless how they access the data file (unless they
have your secret password). The application uses a technique called encryption.
Encryption uses your password to convert notes to unreadable form which makes
no sense to anyone even computer. This can be converted back to readable text
only using exact same password.

#### I forgot my password. How do I recover my notes?

You can't. Your password is not stored anywhere but in your brain. If you lose
your password, you will lose all your notes. Storing password locally or on
server is a security hole which should be avoided.

#### I can't take screenshots from app or I see blank screenshot in application switcher. What's wrong?

You probably have enabled "Secure Window" feature in SealNote settings.
Disable it to be able to take screenshots or show window content in application
switcher.

Contact
-------
Homepage: www.twistedplane.com
Email   : contact@twistedplane.com

Send bugs to the above email with [Sealnote][BUG] tags in subject.
