package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionBatch("code-965", "label-232")
        assertEquals(InspectionBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionBatch("code-965", "label-232")
        try {
            item.moveTo(InspectionBatchStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBatchStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionBatch("code-965", "label-232")
        item.status = InspectionBatchStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionBatchStatus.DRAFT) }
    }
}
