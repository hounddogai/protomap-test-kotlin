package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNearMissBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackNearMissBudget("code-54", "label-626")
        assertEquals(FallbackNearMissBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFallbackNearMissBudget("code-54", "label-626")
        try {
            item.moveTo(FallbackNearMissBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNearMissBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackNearMissBudget("code-54", "label-626")
        item.status = FallbackNearMissBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackNearMissBudgetStatus.PROPOSED) }
    }
}
