package com.acme.transport.emissions.carbonreport.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCarbonReportAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        assertEquals(SeasonalCarbonReportAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        try {
            item.moveTo(SeasonalCarbonReportAgreementStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCarbonReportAgreementStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        item.status = SeasonalCarbonReportAgreementStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalCarbonReportAgreementStatus.NEW) }
    }
}
