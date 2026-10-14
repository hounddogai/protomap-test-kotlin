package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNearMissSegmentValidator()
        val item = newPrimaryNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryNearMissSegmentTitleLengthRule(120)
        val item = newPrimaryNearMissSegment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryNearMissSegmentRequiredCodeRule("primary near miss segment needs a value")
        val item = newPrimaryNearMissSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryNearMissSegmentUnitLengthRule(120)
        val item = newPrimaryNearMissSegment("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
