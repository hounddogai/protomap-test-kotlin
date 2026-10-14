package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTransitTimeEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledTransitTimeEntry("code-239", "label-250")
        assertEquals(ScheduledTransitTimeEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledTransitTimeEntry("code-239", "label-250")
        try {
            item.moveTo(ScheduledTransitTimeEntryStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTransitTimeEntryStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledTransitTimeEntry("code-239", "label-250")
        item.status = ScheduledTransitTimeEntryStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledTransitTimeEntryStatus.DRAFT) }
    }
}
