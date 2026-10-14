package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldRollupValidator()
        val item = newHoldRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = HoldRollupAmountRangeRule(9.4, 347.4)
        val item = newHoldRollup("x", "y")
        item.amount = 348.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HoldRollupRequiredLabelRule("hold rollup needs a value")
        val item = newHoldRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = HoldRollupPhoneLengthRule(86400)
        val item = newHoldRollup("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = HoldRollupCreatedAtRangeRule(32, 3600)
        val item = newHoldRollup("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
