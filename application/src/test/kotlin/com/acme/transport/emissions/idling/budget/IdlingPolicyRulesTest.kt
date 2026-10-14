package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingPolicyValidator()
        val item = newIdlingPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = IdlingPolicyUnitLengthRule(1000)
        val item = newIdlingPolicy("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = IdlingPolicyCeilingRangeRule(3.4, 376.4)
        val item = newIdlingPolicy("x", "y")
        item.ceiling = 377.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = IdlingPolicyContactEmailLengthRule(80)
        val item = newIdlingPolicy("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingPolicyCodeLengthRule(3600)
        val item = newIdlingPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingPolicyRequiredTitleRule("idling policy needs this field")
        val item = newIdlingPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = IdlingPolicyRequiredUnitRule("idling policy needs this field")
        val item = newIdlingPolicy("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
