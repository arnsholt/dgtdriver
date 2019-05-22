/**
 * This package is a slightly modified version of the move generation code
 * from <a href="https://github.com/lichess-org/compression">the lichess
 * compression code</a>.
 *
 * <ul>
 *     <li>The classes Encoder, Huffman and PerftTest have been removed.</li>
 *     <li>Bitboard.rotate180() and Board.rotate180 were added</li>
 *     <li>Board.resultSignal() was added.</li>
 *     <li>The Board class and some of its methods were made public.</li>
 *     <li>MoveList was made to implement Iterable.</li>
 * </ul>
 */
package org.riisholt.dgtdriver.moveparser;
