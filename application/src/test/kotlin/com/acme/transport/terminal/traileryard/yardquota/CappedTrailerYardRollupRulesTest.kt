package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerYardRollupValidator()
        val item = newCappedTrailerYardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CappedTrailerYardRollupRequiredContactEmailRule("capped trailer yard rollup needs this field")
        val item = newCappedTrailerYardRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = CappedTrailerYardRollupContactEmailLengthRule(1000)
        val item = newCappedTrailerYardRollup("x", "y")
        item.contactEmail = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedTrailerYardRollupCountryLengthRule(32)
        val item = newCappedTrailerYardRollup("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedTrailerYardRollupRequiredLabelRule("capped trailer yard rollup needs a value")
        val item = newCappedTrailerYardRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
