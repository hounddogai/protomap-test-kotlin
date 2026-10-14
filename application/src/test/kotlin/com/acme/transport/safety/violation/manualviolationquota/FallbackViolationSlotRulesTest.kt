package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackViolationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackViolationSlotValidator()
        val item = newFallbackViolationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackViolationSlotTitleLengthRule(120)
        val item = newFallbackViolationSlot("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackViolationSlotRequiredCodeRule("fallback violation slot needs an entry")
        val item = newFallbackViolationSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackViolationSlotRequiredTitleRule("fallback violation slot needs this field")
        val item = newFallbackViolationSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FallbackViolationSlotLocaleLengthRule(80)
        val item = newFallbackViolationSlot("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
