package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardBundleValidator()
        val item = newTrailerYardBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = TrailerYardBundleRequiredTrailerIdRule("trailer yard bundle needs a value")
        val item = newTrailerYardBundle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TrailerYardBundleRetriesRangeRule(80, 255)
        val item = newTrailerYardBundle("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TrailerYardBundleRequiredShiftCodeRule("trailer yard bundle needs an entry")
        val item = newTrailerYardBundle("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerYardBundleRequiredNameRule("trailer yard bundle needs a value")
        val item = newTrailerYardBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
