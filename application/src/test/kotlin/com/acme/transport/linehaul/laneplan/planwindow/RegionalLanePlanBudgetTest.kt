package com.acme.transport.linehaul.laneplan.planwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLanePlanBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalLanePlanBudget("code-84", "label-774")
        assertEquals(RegionalLanePlanBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalLanePlanBudget("code-84", "label-774")
        try {
            item.moveTo(RegionalLanePlanBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLanePlanBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalLanePlanBudget("code-84", "label-774")
        item.status = RegionalLanePlanBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalLanePlanBudgetStatus.CREATED) }
    }
}
