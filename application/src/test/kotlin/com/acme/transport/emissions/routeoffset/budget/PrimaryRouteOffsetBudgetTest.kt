package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRouteOffsetBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        assertEquals(PrimaryRouteOffsetBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        try {
            item.moveTo(PrimaryRouteOffsetBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRouteOffsetBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        item.status = PrimaryRouteOffsetBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryRouteOffsetBudgetStatus.CREATED) }
    }
}
