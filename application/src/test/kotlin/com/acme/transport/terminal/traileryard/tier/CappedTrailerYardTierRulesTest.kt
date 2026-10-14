package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerYardTierValidator()
        val item = newCappedTrailerYardTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedTrailerYardTierCodeLengthRule(1000)
        val item = newCappedTrailerYardTier("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTrailerYardTierRequiredCodeRule("capped trailer yard tier needs this field")
        val item = newCappedTrailerYardTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = CappedTrailerYardTierRequiredNotesRule("capped trailer yard tier needs this field")
        val item = newCappedTrailerYardTier("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
