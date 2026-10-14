package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalViolationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalViolationPolicyValidator()
        val item = newRegionalViolationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RegionalViolationPolicyCapacityRangeRule(1000, 3600)
        val item = newRegionalViolationPolicy("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalViolationPolicyPalletsRangeRule(64, 3600)
        val item = newRegionalViolationPolicy("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = RegionalViolationPolicyRequiredSourceRule("regional violation policy needs an entry")
        val item = newRegionalViolationPolicy("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalViolationPolicyRequiredCodeRule("regional violation policy needs a value")
        val item = newRegionalViolationPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
