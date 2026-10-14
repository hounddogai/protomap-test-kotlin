package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCarbonReportAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        assertEquals(FallbackCarbonReportAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        try {
            item.moveTo(FallbackCarbonReportAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCarbonReportAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        item.status = FallbackCarbonReportAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackCarbonReportAgreementStatus.CREATED) }
    }
}
