package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderAssignment("code-966", "label-959")
        assertEquals(TenderAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTenderAssignment("code-966", "label-959")
        try {
            item.moveTo(TenderAssignmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderAssignmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderAssignment("code-966", "label-959")
        item.status = TenderAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderAssignmentStatus.PLANNED) }
    }
}
