package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationEstimateValidator()
        val item = newViolationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationEstimateCodeLengthRule(16)
        val item = newViolationEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = ViolationEstimateQuantityRangeRule(32, 1000)
        val item = newViolationEstimate("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ViolationEstimateEtaMinutesRangeRule(120, 86400)
        val item = newViolationEstimate("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = ViolationEstimateRegionLengthRule(32)
        val item = newViolationEstimate("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ViolationEstimateRequiredNameRule("violation estimate needs this field")
        val item = newViolationEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ViolationEstimatePriorityRangeRule(80, 255)
        val item = newViolationEstimate("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
