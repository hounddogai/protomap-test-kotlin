package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDefectThreshold("code-571", "name-44")
        assertEquals(ScheduledDefectThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledDefectThreshold("code-571", "name-44")
        try {
            item.moveTo(ScheduledDefectThresholdStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectThresholdStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDefectThreshold("code-571", "name-44")
        item.status = ScheduledDefectThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDefectThresholdStatus.DRAFT) }
    }
}
