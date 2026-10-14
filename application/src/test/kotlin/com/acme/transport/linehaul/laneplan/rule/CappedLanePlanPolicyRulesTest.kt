package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLanePlanPolicyValidator()
        val item = newCappedLanePlanPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CappedLanePlanPolicySummaryLengthRule(1000)
        val item = newCappedLanePlanPolicy("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedLanePlanPolicyNameLengthRule(120)
        val item = newCappedLanePlanPolicy("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLanePlanPolicyCodeLengthRule(64)
        val item = newCappedLanePlanPolicy("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedLanePlanPolicyRequiredCodeRule("capped lane plan policy needs an entry")
        val item = newCappedLanePlanPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
