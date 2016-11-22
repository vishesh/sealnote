# SealNote

[![MIT licensed](https://img.shields.io/badge/license-MIT-blue.svg)](COPYING.md)
[![Build Status](https://travis-ci.org/vishesh/sealnote.svg?branch=master)](https://travis-ci.org/vishesh/sealnote)
[![Coverage Status](https://codecov.io/gh/vishesh/sealnote/coverage.svg?branch=master)](https://codecov.io/gh/vishesh/sealnote?branch=master)

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

Contributing
--------------------------------

Contributions are much appreciated. Please go through this section to understand how and what to contribute.

### Tools of trade

1. Java 1.7+
1. Gradle 0.12.2
2. Editor of your choice (Please do not check-in editor project files)

### What to contribute

SealNote is an application which focuses on security and simplicity.

If you need ideas for contribution, please check currently open issues.

If you want to add a new feature, please open a new issue. A good feature can be something which improves some workflow or security for a user.

### Issue assignment

Before you start working on an issue, please claim it so that there is no duplication of effort. We don't assign an issue until you have a passing pull-request.

### How to submit a pull request

1. Clone sealnote:
    `git clone https://github.com/vishesh/sealnote.git && cd sealnote`
2. Verify that build succeeds:
    `./gradlew assembleDebug`
3. Create a topic branch:
    `git checkout -b feature`
4. **Make your changes.**
5. Fork sealnote on GitHub (adds a remote named "YOUR-USER")
7. Push to your fork:
    `git push -u <YOUR-USER> HEAD`
8. Open a pull request describing your changes

### Style Guide

We strive to follow [Google's Java Style Guide](https://google.github.io/styleguide/javaguide.html). If you see any violation, feel free to submit a patch.

### Commit guidelines

We don't have a strict guideline on commit messages. However, we strive to follow [Git-book's](https://git-scm.com/book/ch5-2.html#Commit-Guidelines) commit guidelines for messages.

### Documentation updates

We value documentation and believe in continuous improvement. All the documents can be considered living documents, unless stated otherwise. Please follow the general contribution guidelines when submitting a patch for documents.

Contact
-------
Homepage: www.twistedplane.com
Email   : contact@twistedplane.com

Send bugs to the above email with [Sealnote][BUG] tags in subject.
