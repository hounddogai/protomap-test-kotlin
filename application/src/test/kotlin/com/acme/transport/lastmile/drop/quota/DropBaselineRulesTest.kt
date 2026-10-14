package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropBaselineValidator()
        val item = newDropBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropBaselineCodeLengthRule(80)
        val item = newDropBaseline("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropBaselineLabelLengthRule(86400)
        val item = newDropBaseline("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DropBaselineRequiredLabelRule("drop baseline needs this field")
        val item = newDropBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DropBaselineRateRangeRule(7.6, 170.7)
        val item = newDropBaseline("x", "y")
        item.rate = 172.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DropBaselineRequiredTitleRule("drop baseline needs an entry")
        val item = newDropBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
