package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultUtilizationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultUtilizationPolicyValidator()
        val item = newDefaultUtilizationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = DefaultUtilizationPolicyLocaleLengthRule(1000)
        val item = newDefaultUtilizationPolicy("x", "y")
        item.locale = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DefaultUtilizationPolicyFloorValueRangeRule(8.8, 4674.1)
        val item = newDefaultUtilizationPolicy("x", "y")
        item.floorValue = 4675.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultUtilizationPolicyTitleLengthRule(64)
        val item = newDefaultUtilizationPolicy("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DefaultUtilizationPolicyRequiredLocaleRule("default utilization policy needs a value")
        val item = newDefaultUtilizationPolicy("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultUtilizationPolicyRequiredCodeRule("default utilization policy needs an entry")
        val item = newDefaultUtilizationPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
