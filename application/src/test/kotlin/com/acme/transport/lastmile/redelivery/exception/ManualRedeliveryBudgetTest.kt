package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRedeliveryBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newManualRedeliveryBudget("code-293", "label-490")
        assertEquals(ManualRedeliveryBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualRedeliveryBudget("code-293", "label-490")
        try {
            item.moveTo(ManualRedeliveryBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRedeliveryBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualRedeliveryBudget("code-293", "label-490")
        item.status = ManualRedeliveryBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(ManualRedeliveryBudgetStatus.CREATED) }
    }
}
