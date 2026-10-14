package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledDefectEntry("code-461", "title-374")
        assertEquals(ScheduledDefectEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScheduledDefectEntry("code-461", "title-374")
        try {
            item.moveTo(ScheduledDefectEntryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectEntryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledDefectEntry("code-461", "title-374")
        item.status = ScheduledDefectEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledDefectEntryStatus.PROPOSED) }
    }
}
