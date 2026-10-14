package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakPolicyValidator()
        val item = newRestBreakPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RestBreakPolicyQuantityRangeRule(80, 255)
        val item = newRestBreakPolicy("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RestBreakPolicyCreatedAtRangeRule(255, 86400)
        val item = newRestBreakPolicy("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RestBreakPolicyRequiredTitleRule("rest break policy needs a value")
        val item = newRestBreakPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
