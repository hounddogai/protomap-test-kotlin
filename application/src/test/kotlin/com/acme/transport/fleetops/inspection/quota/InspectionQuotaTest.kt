package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionQuota("code-264", "label-198")
        assertEquals(InspectionQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionQuota("code-264", "label-198")
        try {
            item.moveTo(InspectionQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionQuota("code-264", "label-198")
        item.status = InspectionQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionQuotaStatus.OPEN) }
    }
}
