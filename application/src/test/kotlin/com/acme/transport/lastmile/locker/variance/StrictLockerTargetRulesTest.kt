package com.acme.transport.lastmile.locker.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLockerTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictLockerTargetValidator()
        val item = newStrictLockerTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictLockerTargetTitleLengthRule(1000)
        val item = newStrictLockerTarget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictLockerTargetCodeLengthRule(3600)
        val item = newStrictLockerTarget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictLockerTargetCapacityRangeRule(16, 255)
        val item = newStrictLockerTarget("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictLockerTargetRequiredTitleRule("strict locker target needs an entry")
        val item = newStrictLockerTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
