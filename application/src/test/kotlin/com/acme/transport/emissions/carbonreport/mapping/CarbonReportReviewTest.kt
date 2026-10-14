package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newCarbonReportReview("code-58", "label-877")
        assertEquals(CarbonReportReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCarbonReportReview("code-58", "label-877")
        try {
            item.moveTo(CarbonReportReviewStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportReviewStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCarbonReportReview("code-58", "label-877")
        item.status = CarbonReportReviewStatus.STOPPED
        assertFails("move") { item.moveTo(CarbonReportReviewStatus.IDLE) }
    }
}
