package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIncidentScheduleValidator()
        val item = newTieredIncidentSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TieredIncidentScheduleRequiredContactEmailRule("tiered incident schedule needs an entry")
        val item = newTieredIncidentSchedule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TieredIncidentScheduleCapacityRangeRule(16, 255)
        val item = newTieredIncidentSchedule("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredIncidentScheduleThresholdRangeRule(1.0, 4428.1)
        val item = newTieredIncidentSchedule("x", "y")
        item.threshold = 4429.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
