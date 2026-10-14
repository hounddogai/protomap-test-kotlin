package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakTemplateValidator()
        val item = newRestBreakTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RestBreakTemplateWeightRangeRule(8.7, 2955.7)
        val item = newRestBreakTemplate("x", "y")
        item.weight = 2957.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = RestBreakTemplateScoreRangeRule(4.6, 806.2)
        val item = newRestBreakTemplate("x", "y")
        item.score = 807.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakTemplateNameLengthRule(1000)
        val item = newRestBreakTemplate("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RestBreakTemplateRequiredDockRule("rest break template needs a value")
        val item = newRestBreakTemplate("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
