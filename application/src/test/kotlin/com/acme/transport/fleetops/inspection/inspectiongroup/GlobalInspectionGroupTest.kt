package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalInspectionGroup("code-730", "name-355")
        assertEquals(GlobalInspectionGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newGlobalInspectionGroup("code-730", "name-355")
        try {
            item.moveTo(GlobalInspectionGroupStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionGroupStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalInspectionGroup("code-730", "name-355")
        item.status = GlobalInspectionGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalInspectionGroupStatus.PLANNED) }
    }
}
