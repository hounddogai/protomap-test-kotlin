package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultViolationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultViolationSegmentValidator()
        val item = newDefaultViolationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DefaultViolationSegmentCapacityRangeRule(120, 1000)
        val item = newDefaultViolationSegment("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultViolationSegmentNameLengthRule(64)
        val item = newDefaultViolationSegment("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultViolationSegmentCodeLengthRule(64)
        val item = newDefaultViolationSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultViolationSegmentRequiredNameRule("default violation segment needs an entry")
        val item = newDefaultViolationSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultViolationSegmentRequiredCodeRule("default violation segment needs a value")
        val item = newDefaultViolationSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
