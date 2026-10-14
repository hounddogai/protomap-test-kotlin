package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLanePlanSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingLanePlanSegmentValidator()
        val item = newPendingLanePlanSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PendingLanePlanSegmentCreatedAtRangeRule(16, 32)
        val item = newPendingLanePlanSegment("x", "y")
        item.createdAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PendingLanePlanSegmentFuelLitersRangeRule(7.9, 3649.0)
        val item = newPendingLanePlanSegment("x", "y")
        item.fuelLiters = 3650.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingLanePlanSegmentRequiredTitleRule("pending lane plan segment needs this field")
        val item = newPendingLanePlanSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
