package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientIncidentAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        assertEquals(LenientIncidentAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        try {
            item.moveTo(LenientIncidentAdjustmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientIncidentAdjustmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        item.status = LenientIncidentAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(LenientIncidentAdjustmentStatus.NEW) }
    }
}
