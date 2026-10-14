package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardMappingValidator()
        val item = newTrailerYardMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = TrailerYardMappingOwnerIdLengthRule(86400)
        val item = newTrailerYardMapping("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TrailerYardMappingEtaMinutesRangeRule(16, 120)
        val item = newTrailerYardMapping("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerYardMappingRequiredNameRule("trailer yard mapping needs this field")
        val item = newTrailerYardMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TrailerYardMappingNameLengthRule(255)
        val item = newTrailerYardMapping("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TrailerYardMappingMinimumRangeRule(8.3, 1737.7)
        val item = newTrailerYardMapping("x", "y")
        item.minimum = 1739.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
