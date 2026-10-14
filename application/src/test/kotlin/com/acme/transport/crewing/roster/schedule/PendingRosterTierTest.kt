package com.acme.transport.crewing.roster.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRosterTierTest {
    @Test
    fun `starts in open`() {
        val item = newPendingRosterTier("code-1", "name-379")
        assertEquals(PendingRosterTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingRosterTier("code-1", "name-379")
        try {
            item.moveTo(PendingRosterTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRosterTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingRosterTier("code-1", "name-379")
        item.status = PendingRosterTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingRosterTierStatus.OPEN) }
    }
}
