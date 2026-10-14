package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerBudget("code-308", "label-387")
        assertEquals(TrailerBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTrailerBudget("code-308", "label-387")
        try {
            item.moveTo(TrailerBudgetStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerBudgetStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerBudget("code-308", "label-387")
        item.status = TrailerBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerBudgetStatus.CREATED) }
    }
}
