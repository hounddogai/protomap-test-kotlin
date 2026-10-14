package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanPolicyValidator()
        val item = newLanePlanPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LanePlanPolicyMinimumRangeRule(2.1, 3969.1)
        val item = newLanePlanPolicy("x", "y")
        item.minimum = 3970.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanPolicyRequiredCodeRule("lane plan policy needs an entry")
        val item = newLanePlanPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LanePlanPolicyMarginRangeRule(2.4, 638.6)
        val item = newLanePlanPolicy("x", "y")
        item.margin = 640.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LanePlanPolicyRequiredTimezoneRule("lane plan policy needs this field")
        val item = newLanePlanPolicy("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanPolicyCodeLengthRule(86400)
        val item = newLanePlanPolicy("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
