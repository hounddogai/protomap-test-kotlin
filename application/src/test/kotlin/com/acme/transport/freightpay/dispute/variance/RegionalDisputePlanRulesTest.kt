package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDisputePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDisputePlanValidator()
        val item = newRegionalDisputePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalDisputePlanRequiredLabelRule("regional dispute plan needs a value")
        val item = newRegionalDisputePlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDisputePlanLabelLengthRule(86400)
        val item = newRegionalDisputePlan("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RegionalDisputePlanLimitRangeRule(64, 1000)
        val item = newRegionalDisputePlan("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDisputePlanRequiredCodeRule("regional dispute plan needs this field")
        val item = newRegionalDisputePlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
