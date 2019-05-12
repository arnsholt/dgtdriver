/**
 * This package is a modified version of the move generation code from
 * <a href="https://github.com/lichess-org/compression">the lichess
 * compression code</a>. To suit our purposes, a number of changes have been
 * made to {@link org.riisholt.dgtdriver.moveparser.Board Board}:
 *
 * <ul>
 *     <li>The side to move is no longer tracked. Instead, the move generation
 *     code takes the side to move as an argument.</li>
 *     <li>TODO: The en passant square is no longer tracked, instead ep
 *     captures are allowed whenever pawns are in the appropriate
 *     configuration.</li>
 *     <li>TODO: Castling is allowed whenever a rook and king are on their
 *     starting squares.</li>
 * </ul>
 */
package org.riisholt.dgtdriver.moveparser;