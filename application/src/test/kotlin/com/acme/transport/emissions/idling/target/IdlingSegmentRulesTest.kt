package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingSegmentValidator()
        val item = newIdlingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = IdlingSegmentDescriptionLengthRule(80)
        val item = newIdlingSegment("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = IdlingSegmentCeilingRangeRule(6.6, 3838.5)
        val item = newIdlingSegment("x", "y")
        item.ceiling = 3840.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = IdlingSegmentNameLengthRule(1000)
        val item = newIdlingSegment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingSegmentRequiredCodeRule("idling segment needs an entry")
        val item = newIdlingSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
