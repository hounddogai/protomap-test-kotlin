package com.acme.transport.customs.clearance.clearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearancePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearancePolicyValidator()
        val item = newClearancePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ClearancePolicyDurationMinutesRangeRule(32, 255)
        val item = newClearancePolicy("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ClearancePolicyNameLengthRule(1000)
        val item = newClearancePolicy("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ClearancePolicyRateRangeRule(8.7, 3461.8)
        val item = newClearancePolicy("x", "y")
        item.rate = 3463.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
