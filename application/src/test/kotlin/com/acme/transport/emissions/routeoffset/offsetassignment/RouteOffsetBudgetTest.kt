package com.acme.transport.emissions.routeoffset.offsetassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newRouteOffsetBudget("code-608", "label-9")
        assertEquals(RouteOffsetBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRouteOffsetBudget("code-608", "label-9")
        try {
            item.moveTo(RouteOffsetBudgetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetBudgetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRouteOffsetBudget("code-608", "label-9")
        item.status = RouteOffsetBudgetStatus.FAILED
        assertFails("move") { item.moveTo(RouteOffsetBudgetStatus.NEW) }
    }
}
