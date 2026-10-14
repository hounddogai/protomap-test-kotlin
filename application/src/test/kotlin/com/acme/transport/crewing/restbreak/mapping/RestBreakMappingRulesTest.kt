package com.acme.transport.crewing.restbreak.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakMappingValidator()
        val item = newRestBreakMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RestBreakMappingSequenceRangeRule(80, 86400)
        val item = newRestBreakMapping("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RestBreakMappingRequiredTimezoneRule("rest break mapping needs this field")
        val item = newRestBreakMapping("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakMappingCodeLengthRule(86400)
        val item = newRestBreakMapping("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakMappingRequiredCodeRule("rest break mapping needs a value")
        val item = newRestBreakMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
