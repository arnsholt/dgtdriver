# DgtDriver

This package is a pure-Java implementation of the Digital Game Technology
e-board serial protocol. Note however that the underlying interaction with the
serial port is _not_ handled here, rather the code using the library must
supply data to the driver as events happen on the serial port. This way, the
library is not tied to a single conception of serial port interaction and is
more widely usable. See https://github.com/arnsholt/dgtpgn for a simple
application example including serial port interaction.

The protocol implementation here has been implemented from DGT's description
of the protocol in the `dgtbrd.h` C header file. I have not been able to find
this file on the current DGT webpages, but instead relied on an archived copy
from the Internet Archive's wayback machine:
http://www.dgtprojects.com/site/index.php/dgtsupport/developer-info/downloads/doc_download/85-dgt-electronic-board-protocol-description-version-20120309

While the most important parts of the protocol have been implemented, some
parts are still outstanding:

* Draughts boards are not supported.
* The DGT_EE_MOVES response (dump of moves stored in EEPROM) is currently
  silently ignored and the exact API for handling this is to be determined.
* Sending commands to the clock is not yet implemented.
* Bus mode is not supported.
