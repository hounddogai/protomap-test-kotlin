package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNearMissBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryNearMissBudget("code-348", "title-571")
        assertEquals(PrimaryNearMissBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPrimaryNearMissBudget("code-348", "title-571")
        try {
            item.moveTo(PrimaryNearMissBudgetStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNearMissBudgetStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryNearMissBudget("code-348", "title-571")
        item.status = PrimaryNearMissBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryNearMissBudgetStatus.IDLE) }
    }
}
