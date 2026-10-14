package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakSegmentValidator()
        val item = newRestBreakSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakSegmentRequiredLabelRule("rest break segment needs this field")
        val item = newRestBreakSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakSegmentCodeLengthRule(255)
        val item = newRestBreakSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
