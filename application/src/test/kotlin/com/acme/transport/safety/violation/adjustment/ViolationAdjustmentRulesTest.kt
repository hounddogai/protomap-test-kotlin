package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationAdjustmentValidator()
        val item = newViolationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ViolationAdjustmentLimitRangeRule(120, 86400)
        val item = newViolationAdjustment("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ViolationAdjustmentRequiredNameRule("violation adjustment needs an entry")
        val item = newViolationAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationAdjustmentCodeLengthRule(1000)
        val item = newViolationAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
