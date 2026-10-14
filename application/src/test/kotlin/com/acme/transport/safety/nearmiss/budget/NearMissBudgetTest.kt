package com.acme.transport.safety.nearmiss.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newNearMissBudget("code-35", "name-10")
        assertEquals(NearMissBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newNearMissBudget("code-35", "name-10")
        try {
            item.moveTo(NearMissBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNearMissBudget("code-35", "name-10")
        item.status = NearMissBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(NearMissBudgetStatus.IDLE) }
    }
}
