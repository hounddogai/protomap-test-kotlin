package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        assertEquals(RegionalCarbonReportRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        try {
            item.moveTo(RegionalCarbonReportRuleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportRuleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        item.status = RegionalCarbonReportRuleStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalCarbonReportRuleStatus.DRAFT) }
    }
}
