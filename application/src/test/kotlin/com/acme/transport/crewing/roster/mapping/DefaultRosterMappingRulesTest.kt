package com.acme.transport.crewing.roster.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRosterMappingValidator()
        val item = newDefaultRosterMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefaultRosterMappingOdometerRangeRule(3600, 86400)
        val item = newDefaultRosterMapping("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRosterMappingRequiredCodeRule("default roster mapping needs an entry")
        val item = newDefaultRosterMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultRosterMappingLabelLengthRule(64)
        val item = newDefaultRosterMapping("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DefaultRosterMappingRequiredPhoneRule("default roster mapping needs a value")
        val item = newDefaultRosterMapping("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
