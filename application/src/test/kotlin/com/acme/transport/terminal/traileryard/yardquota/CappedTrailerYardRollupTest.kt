package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerYardRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedTrailerYardRollup("code-220", "label-545")
        assertEquals(CappedTrailerYardRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedTrailerYardRollup("code-220", "label-545")
        try {
            item.moveTo(CappedTrailerYardRollupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerYardRollupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedTrailerYardRollup("code-220", "label-545")
        item.status = CappedTrailerYardRollupStatus.CLOSED
        assertFails("move") { item.moveTo(CappedTrailerYardRollupStatus.DRAFT) }
    }
}
