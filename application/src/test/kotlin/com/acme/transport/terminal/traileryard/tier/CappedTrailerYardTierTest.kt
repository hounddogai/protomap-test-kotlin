package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerYardTierTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedTrailerYardTier("code-237", "title-441")
        assertEquals(CappedTrailerYardTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCappedTrailerYardTier("code-237", "title-441")
        try {
            item.moveTo(CappedTrailerYardTierStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerYardTierStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedTrailerYardTier("code-237", "title-441")
        item.status = CappedTrailerYardTierStatus.STOPPED
        assertFails("move") { item.moveTo(CappedTrailerYardTierStatus.IDLE) }
    }
}
