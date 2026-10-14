package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledInspectionScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledInspectionScore("code-572", "name-691")
        assertEquals(ScheduledInspectionScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledInspectionScore("code-572", "name-691")
        try {
            item.moveTo(ScheduledInspectionScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledInspectionScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledInspectionScore("code-572", "name-691")
        item.status = ScheduledInspectionScoreStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledInspectionScoreStatus.IDLE) }
    }
}
