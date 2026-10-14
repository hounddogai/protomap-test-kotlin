package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionQuota("code-51", "label-627")
        assertEquals(InspectionQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionQuota("code-51", "label-627")
        try {
            item.moveTo(InspectionQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionQuota("code-51", "label-627")
        item.status = InspectionQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionQuotaStatus.OPEN) }
    }
}
