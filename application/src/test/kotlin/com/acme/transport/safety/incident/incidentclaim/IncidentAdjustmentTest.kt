package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentAdjustment("code-887", "label-17")
        assertEquals(IncidentAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newIncidentAdjustment("code-887", "label-17")
        try {
            item.moveTo(IncidentAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentAdjustment("code-887", "label-17")
        item.status = IncidentAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentAdjustmentStatus.PROPOSED) }
    }
}
