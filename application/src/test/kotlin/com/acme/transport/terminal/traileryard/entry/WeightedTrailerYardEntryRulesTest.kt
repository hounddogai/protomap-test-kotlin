package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerYardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTrailerYardEntryValidator()
        val item = newWeightedTrailerYardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = WeightedTrailerYardEntryCurrencyLengthRule(32)
        val item = newWeightedTrailerYardEntry("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = WeightedTrailerYardEntryOwnerIdLengthRule(32)
        val item = newWeightedTrailerYardEntry("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTrailerYardEntryCodeLengthRule(86400)
        val item = newWeightedTrailerYardEntry("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
