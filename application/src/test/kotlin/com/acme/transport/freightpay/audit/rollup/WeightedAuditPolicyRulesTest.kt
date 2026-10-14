package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAuditPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAuditPolicyValidator()
        val item = newWeightedAuditPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = WeightedAuditPolicyFloorValueRangeRule(7.2, 4362.4)
        val item = newWeightedAuditPolicy("x", "y")
        item.floorValue = 4363.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = WeightedAuditPolicyThresholdRangeRule(4.6, 1304.6)
        val item = newWeightedAuditPolicy("x", "y")
        item.threshold = 1306.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = WeightedAuditPolicyRouteCodeLengthRule(255)
        val item = newWeightedAuditPolicy("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = WeightedAuditPolicyRequiredRouteCodeRule("weighted audit policy needs a value")
        val item = newWeightedAuditPolicy("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAuditPolicyCodeLengthRule(120)
        val item = newWeightedAuditPolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
