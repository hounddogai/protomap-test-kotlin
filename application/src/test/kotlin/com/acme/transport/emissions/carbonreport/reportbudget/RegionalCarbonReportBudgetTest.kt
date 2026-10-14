package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        assertEquals(RegionalCarbonReportBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        try {
            item.moveTo(RegionalCarbonReportBudgetStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportBudgetStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalCarbonReportBudget("code-161", "label-817")
        item.status = RegionalCarbonReportBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalCarbonReportBudgetStatus.DRAFT) }
    }
}
