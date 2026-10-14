package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCarbonReportExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newPendingCarbonReportException("code-7", "title-890")
        assertEquals(PendingCarbonReportExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingCarbonReportException("code-7", "title-890")
        try {
            item.moveTo(PendingCarbonReportExceptionStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCarbonReportExceptionStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingCarbonReportException("code-7", "title-890")
        item.status = PendingCarbonReportExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(PendingCarbonReportExceptionStatus.CREATED) }
    }
}
