package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalInspectionGroup("code-824", "label-437")
        assertEquals(GlobalInspectionGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalInspectionGroup("code-824", "label-437")
        try {
            item.moveTo(GlobalInspectionGroupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionGroupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalInspectionGroup("code-824", "label-437")
        item.status = GlobalInspectionGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalInspectionGroupStatus.PLANNED) }
    }
}
