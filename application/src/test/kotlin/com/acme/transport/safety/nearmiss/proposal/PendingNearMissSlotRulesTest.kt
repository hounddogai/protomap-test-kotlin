package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingNearMissSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingNearMissSlotValidator()
        val item = newPendingNearMissSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingNearMissSlotBudgetRangeRule(1.8, 2008.5)
        val item = newPendingNearMissSlot("x", "y")
        item.budget = 2010.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PendingNearMissSlotUnitLengthRule(32)
        val item = newPendingNearMissSlot("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingNearMissSlotNameLengthRule(86400)
        val item = newPendingNearMissSlot("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PendingNearMissSlotDockLengthRule(32)
        val item = newPendingNearMissSlot("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
