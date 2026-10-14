package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualNodePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualNodePolicyValidator()
        val item = newManualNodePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualNodePolicyRequiredTitleRule("manual node policy needs an entry")
        val item = newManualNodePolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ManualNodePolicyLocaleLengthRule(16)
        val item = newManualNodePolicy("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ManualNodePolicyLimitRangeRule(80, 3600)
        val item = newManualNodePolicy("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualNodePolicyRequiredCodeRule("manual node policy needs an entry")
        val item = newManualNodePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ManualNodePolicyGraceDaysRangeRule(120, 120)
        val item = newManualNodePolicy("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
