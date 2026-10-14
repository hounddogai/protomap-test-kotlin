package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerYardSnapshot("code-997", "name-917")
        assertEquals(TrailerYardSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTrailerYardSnapshot("code-997", "name-917")
        try {
            item.moveTo(TrailerYardSnapshotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardSnapshotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerYardSnapshot("code-997", "name-917")
        item.status = TrailerYardSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerYardSnapshotStatus.DRAFT) }
    }
}
