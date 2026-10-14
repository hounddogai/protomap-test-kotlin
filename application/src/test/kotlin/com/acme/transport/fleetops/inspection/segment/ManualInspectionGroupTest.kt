package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newManualInspectionGroup("code-808", "name-2")
        assertEquals(ManualInspectionGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newManualInspectionGroup("code-808", "name-2")
        try {
            item.moveTo(ManualInspectionGroupStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionGroupStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualInspectionGroup("code-808", "name-2")
        item.status = ManualInspectionGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualInspectionGroupStatus.PLANNED) }
    }
}
