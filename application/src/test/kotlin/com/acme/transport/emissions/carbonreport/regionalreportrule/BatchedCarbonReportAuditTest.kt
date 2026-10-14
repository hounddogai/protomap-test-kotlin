package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCarbonReportAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedCarbonReportAudit("code-838", "name-617")
        assertEquals(BatchedCarbonReportAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBatchedCarbonReportAudit("code-838", "name-617")
        try {
            item.moveTo(BatchedCarbonReportAuditStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCarbonReportAuditStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedCarbonReportAudit("code-838", "name-617")
        item.status = BatchedCarbonReportAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedCarbonReportAuditStatus.PLANNED) }
    }
}
