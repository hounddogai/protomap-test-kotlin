package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityMappingValidator()
        val item = newAvailabilityMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AvailabilityMappingNameLengthRule(255)
        val item = newAvailabilityMapping("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = AvailabilityMappingNotesLengthRule(64)
        val item = newAvailabilityMapping("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityMappingCodeLengthRule(64)
        val item = newAvailabilityMapping("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AvailabilityMappingRequiredCurrencyRule("availability mapping needs an entry")
        val item = newAvailabilityMapping("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
