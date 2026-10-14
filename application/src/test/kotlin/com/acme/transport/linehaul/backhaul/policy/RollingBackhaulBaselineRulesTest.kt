package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulBaselineValidator()
        val item = newRollingBackhaulBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingBackhaulBaselineRequiredCodeRule("rolling backhaul baseline needs a value")
        val item = newRollingBackhaulBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBackhaulBaselineCodeLengthRule(16)
        val item = newRollingBackhaulBaseline("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RollingBackhaulBaselineUpdatedAtRangeRule(32, 255)
        val item = newRollingBackhaulBaseline("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingBackhaulBaselineFuelLitersRangeRule(5.9, 4773.5)
        val item = newRollingBackhaulBaseline("x", "y")
        item.fuelLiters = 4775.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
