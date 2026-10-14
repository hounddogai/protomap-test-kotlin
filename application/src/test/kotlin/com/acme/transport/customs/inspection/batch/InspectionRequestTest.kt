package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionRequest("code-991", "title-286")
        assertEquals(InspectionRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionRequest("code-991", "title-286")
        try {
            item.moveTo(InspectionRequestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRequestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionRequest("code-991", "title-286")
        item.status = InspectionRequestStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionRequestStatus.DRAFT) }
    }
}
