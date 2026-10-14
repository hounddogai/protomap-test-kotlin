package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportAgreement("code-845", "title-440")
        assertEquals(CarbonReportAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCarbonReportAgreement("code-845", "title-440")
        try {
            item.moveTo(CarbonReportAgreementStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportAgreementStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportAgreement("code-845", "title-440")
        item.status = CarbonReportAgreementStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportAgreementStatus.NEW) }
    }
}
