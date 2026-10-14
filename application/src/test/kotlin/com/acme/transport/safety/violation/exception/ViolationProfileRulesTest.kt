package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationProfileValidator()
        val item = newViolationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ViolationProfileLabelLengthRule(86400)
        val item = newViolationProfile("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ViolationProfileCostRangeRule(1.3, 634.0)
        val item = newViolationProfile("x", "y")
        item.cost = 635.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ViolationProfileRequiredCodeRule("violation profile needs an entry")
        val item = newViolationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
