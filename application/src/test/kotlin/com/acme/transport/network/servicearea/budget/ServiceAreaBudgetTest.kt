package com.acme.transport.network.servicearea.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaBudget("code-505", "label-282")
        assertEquals(ServiceAreaBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newServiceAreaBudget("code-505", "label-282")
        try {
            item.moveTo(ServiceAreaBudgetStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaBudgetStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaBudget("code-505", "label-282")
        item.status = ServiceAreaBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaBudgetStatus.DRAFT) }
    }
}
