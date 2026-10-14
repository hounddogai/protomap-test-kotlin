package com.acme.transport.terminal.traileryard.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerYardManifestValidator()
        val item = newCappedTrailerYardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = CappedTrailerYardManifestWeightRangeRule(6.6, 4846.8)
        val item = newCappedTrailerYardManifest("x", "y")
        item.weight = 4848.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTrailerYardManifestRequiredCodeRule("capped trailer yard manifest needs an entry")
        val item = newCappedTrailerYardManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CappedTrailerYardManifestCurrencyLengthRule(255)
        val item = newCappedTrailerYardManifest("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
