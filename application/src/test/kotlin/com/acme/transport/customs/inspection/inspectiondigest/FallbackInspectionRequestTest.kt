package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionRequestTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackInspectionRequest("code-898", "label-12")
        assertEquals(FallbackInspectionRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackInspectionRequest("code-898", "label-12")
        try {
            item.moveTo(FallbackInspectionRequestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionRequestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackInspectionRequest("code-898", "label-12")
        item.status = FallbackInspectionRequestStatus.FAILED
        assertFails("move") { item.moveTo(FallbackInspectionRequestStatus.NEW) }
    }
}
