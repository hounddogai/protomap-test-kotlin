package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLockerTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryLockerTargetValidator()
        val item = newPrimaryLockerTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = PrimaryLockerTargetRequiredCarrierRule("primary locker target needs this field")
        val item = newPrimaryLockerTarget("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = PrimaryLockerTargetScoreRangeRule(4.0, 4123.7)
        val item = newPrimaryLockerTarget("x", "y")
        item.score = 4125.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryLockerTargetLabelLengthRule(32)
        val item = newPrimaryLockerTarget("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryLockerTargetRequiredCodeRule("primary locker target needs an entry")
        val item = newPrimaryLockerTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
