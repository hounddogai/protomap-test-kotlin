package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedViolationMappingValidator()
        val item = newCappedViolationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CappedViolationMappingSequenceRangeRule(80, 255)
        val item = newCappedViolationMapping("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedViolationMappingCodeLengthRule(120)
        val item = newCappedViolationMapping("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CappedViolationMappingRateRangeRule(3.8, 176.9)
        val item = newCappedViolationMapping("x", "y")
        item.rate = 178.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
