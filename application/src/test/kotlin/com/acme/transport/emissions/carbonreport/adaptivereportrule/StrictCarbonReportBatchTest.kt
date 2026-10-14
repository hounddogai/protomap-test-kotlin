package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCarbonReportBatchTest {
    @Test
    fun `starts in open`() {
        val item = newStrictCarbonReportBatch("code-321", "title-73")
        assertEquals(StrictCarbonReportBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictCarbonReportBatch("code-321", "title-73")
        try {
            item.moveTo(StrictCarbonReportBatchStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCarbonReportBatchStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictCarbonReportBatch("code-321", "title-73")
        item.status = StrictCarbonReportBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictCarbonReportBatchStatus.OPEN) }
    }
}
