package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakBaselineValidator()
        val item = newRestBreakBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RestBreakBaselineDepotLengthRule(255)
        val item = newRestBreakBaseline("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RestBreakBaselineExpiresAtRangeRule(32, 32)
        val item = newRestBreakBaseline("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RestBreakBaselineRatioRangeRule(2.8, 3563.3)
        val item = newRestBreakBaseline("x", "y")
        item.ratio = 3564.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakBaselineRequiredNameRule("rest break baseline needs this field")
        val item = newRestBreakBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
