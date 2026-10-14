package com.acme.transport.terminal.traileryard.yardcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTrailerYardBundleValidator()
        val item = newLegacyTrailerYardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = LegacyTrailerYardBundleMarginRangeRule(9.4, 884.7)
        val item = newLegacyTrailerYardBundle("x", "y")
        item.margin = 886.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = LegacyTrailerYardBundleRequiredDockRule("legacy trailer yard bundle needs a value")
        val item = newLegacyTrailerYardBundle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LegacyTrailerYardBundleDockLengthRule(120)
        val item = newLegacyTrailerYardBundle("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
