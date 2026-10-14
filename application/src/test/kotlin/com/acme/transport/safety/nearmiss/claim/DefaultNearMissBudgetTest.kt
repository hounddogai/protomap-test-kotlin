package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNearMissBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultNearMissBudget("code-63", "name-195")
        assertEquals(DefaultNearMissBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultNearMissBudget("code-63", "name-195")
        try {
            item.moveTo(DefaultNearMissBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNearMissBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultNearMissBudget("code-63", "name-195")
        item.status = DefaultNearMissBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultNearMissBudgetStatus.IDLE) }
    }
}
