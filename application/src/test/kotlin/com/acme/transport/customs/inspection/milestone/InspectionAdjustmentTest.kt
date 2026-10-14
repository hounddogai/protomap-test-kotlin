package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionAdjustment("code-836", "label-333")
        assertEquals(InspectionAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newInspectionAdjustment("code-836", "label-333")
        try {
            item.moveTo(InspectionAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionAdjustment("code-836", "label-333")
        item.status = InspectionAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(InspectionAdjustmentStatus.NEW) }
    }
}
