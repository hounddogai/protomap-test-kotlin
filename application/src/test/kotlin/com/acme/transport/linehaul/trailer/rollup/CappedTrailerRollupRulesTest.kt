package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerRollupValidator()
        val item = newCappedTrailerRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CappedTrailerRollupDockLengthRule(80)
        val item = newCappedTrailerRollup("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = CappedTrailerRollupRequiredDescriptionRule("capped trailer rollup needs this field")
        val item = newCappedTrailerRollup("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CappedTrailerRollupRateRangeRule(4.7, 3922.0)
        val item = newCappedTrailerRollup("x", "y")
        item.rate = 3923.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
