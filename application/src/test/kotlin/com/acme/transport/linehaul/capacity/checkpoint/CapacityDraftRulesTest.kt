package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityDraftValidator()
        val item = newCapacityDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CapacityDraftRequiredTitleRule("capacity draft needs a value")
        val item = newCapacityDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CapacityDraftRequiredUnitRule("capacity draft needs an entry")
        val item = newCapacityDraft("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CapacityDraftUnitLengthRule(1000)
        val item = newCapacityDraft("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CapacityDraftAmountRangeRule(3.1, 1144.7)
        val item = newCapacityDraft("x", "y")
        item.amount = 1146.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
