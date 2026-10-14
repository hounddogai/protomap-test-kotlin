package com.acme.transport.safety.nearmiss.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyNearMissBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyNearMissBudget("code-795", "label-797")
        assertEquals(LegacyNearMissBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyNearMissBudget("code-795", "label-797")
        try {
            item.moveTo(LegacyNearMissBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyNearMissBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyNearMissBudget("code-795", "label-797")
        item.status = LegacyNearMissBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyNearMissBudgetStatus.REQUESTED) }
    }
}
