package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterTierTest {
    @Test
    fun `starts in idle`() {
        val item = newRosterTier("code-265", "title-32")
        assertEquals(RosterTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRosterTier("code-265", "title-32")
        try {
            item.moveTo(RosterTierStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterTierStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRosterTier("code-265", "title-32")
        item.status = RosterTierStatus.STOPPED
        assertFails("move") { item.moveTo(RosterTierStatus.IDLE) }
    }
}
