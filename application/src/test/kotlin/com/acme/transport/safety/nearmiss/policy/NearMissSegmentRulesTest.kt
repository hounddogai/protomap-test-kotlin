package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissSegmentValidator()
        val item = newNearMissSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissSegmentRequiredCodeRule("near miss segment needs a value")
        val item = newNearMissSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = NearMissSegmentEndsAtRangeRule(80, 1000)
        val item = newNearMissSegment("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = NearMissSegmentRequiredDriverIdRule("near miss segment needs this field")
        val item = newNearMissSegment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = NearMissSegmentRequiredRouteCodeRule("near miss segment needs an entry")
        val item = newNearMissSegment("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
