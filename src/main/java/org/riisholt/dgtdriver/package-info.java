/**
 * <p>This library started its life in an effort to write a simple Android app
 * for recording games off a DGT board. That project quickly became unworkable
 * when the development loop involved working with a physical board and phone,
 * which was just painfully slow. This drives one of the first decisions of
 * this library, namely that only the wire <em>protocol</em> is implemented;
 * the actual reading and writing of bytes to the serial interface itself is a
 * separate concern, so that the same code can be used in Android and standard
 * Java applications.</p>
 *
 * <p>A second important choice is that the objects provided are generally in
 * an immutable, value object style where all public data is provided as
 * {@code public final} members that can be accessed without an intervening
 * getter, and modifying an object requires you to construct a modified copy;
 * directly modifying domain objects is not supported.</p>
 *
 * <h2>Some details about DBT boards</h2>
 * <p>For details on the electronic guts of the DGT board, I refer you to <a
 * href="https://www.chessprogramming.org/DGT_Board">the Chess Programming
 * Wiki</a>, but for working with the board some idea of how the hardware
 * looks at the world is necessary. First of all, the DGT board has a "right"
 * and a "wrong" way around; that is, A1 is a determined physical square on
 * the board, independently of where the white and black pieces are placed.
 * Specifically, if you sit in front of the board with the connectors on the
 * left-hand side, A1 is the bottom-left square and H8 the top-right. Along
 * the same lines, information about the clock is given simply as the time on
 * the left-hand and right-hand clocks. There's nothing stopping you from
 * playing in other configurations, but all data is reported in these
 * coordinates, so higher-level code needs to handle this somehow. See for
 * example {@link org.riisholt.dgtdriver.moveparser.MoveParser} for how it
 * detects the orientation of the game.</p>
 */
package org.riisholt.dgtdriver;
