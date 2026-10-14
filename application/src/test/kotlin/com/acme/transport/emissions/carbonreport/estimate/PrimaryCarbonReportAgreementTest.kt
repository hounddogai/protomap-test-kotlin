package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCarbonReportAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryCarbonReportAgreement("code-404", "label-662")
        assertEquals(PrimaryCarbonReportAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPrimaryCarbonReportAgreement("code-404", "label-662")
        try {
            item.moveTo(PrimaryCarbonReportAgreementStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCarbonReportAgreementStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryCarbonReportAgreement("code-404", "label-662")
        item.status = PrimaryCarbonReportAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryCarbonReportAgreementStatus.OPEN) }
    }
}
