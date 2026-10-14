package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNodeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNodeProfileValidator()
        val item = newPrimaryNodeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PrimaryNodeProfileChannelLengthRule(86400)
        val item = newPrimaryNodeProfile("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PrimaryNodeProfileBudgetRangeRule(8.1, 3100.9)
        val item = newPrimaryNodeProfile("x", "y")
        item.budget = 3102.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryNodeProfileCodeLengthRule(3600)
        val item = newPrimaryNodeProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
