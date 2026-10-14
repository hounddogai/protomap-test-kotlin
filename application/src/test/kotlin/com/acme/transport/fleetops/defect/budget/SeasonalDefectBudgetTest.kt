package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDefectBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalDefectBudget("code-380", "title-806")
        assertEquals(SeasonalDefectBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalDefectBudget("code-380", "title-806")
        try {
            item.moveTo(SeasonalDefectBudgetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDefectBudgetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalDefectBudget("code-380", "title-806")
        item.status = SeasonalDefectBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalDefectBudgetStatus.PLANNED) }
    }
}
