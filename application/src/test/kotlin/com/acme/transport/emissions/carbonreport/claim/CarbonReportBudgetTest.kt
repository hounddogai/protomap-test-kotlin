package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newCarbonReportBudget("code-64", "name-849")
        assertEquals(CarbonReportBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCarbonReportBudget("code-64", "name-849")
        try {
            item.moveTo(CarbonReportBudgetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportBudgetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCarbonReportBudget("code-64", "name-849")
        item.status = CarbonReportBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(CarbonReportBudgetStatus.REQUESTED) }
    }
}
