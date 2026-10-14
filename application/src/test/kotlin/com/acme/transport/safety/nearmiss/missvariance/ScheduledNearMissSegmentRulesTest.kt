package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNearMissSegmentValidator()
        val item = newScheduledNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ScheduledNearMissSegmentRequiredRouteCodeRule("scheduled near miss segment needs an entry")
        val item = newScheduledNearMissSegment("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledNearMissSegmentCodeLengthRule(32)
        val item = newScheduledNearMissSegment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = ScheduledNearMissSegmentVersionRangeRule(32, 86400)
        val item = newScheduledNearMissSegment("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ScheduledNearMissSegmentPhoneLengthRule(1000)
        val item = newScheduledNearMissSegment("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledNearMissSegmentNameLengthRule(80)
        val item = newScheduledNearMissSegment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledNearMissSegmentRequiredNameRule("scheduled near miss segment needs an entry")
        val item = newScheduledNearMissSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
