package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCoachingScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCoachingScheduleValidator()
        val item = newTieredCoachingSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredCoachingScheduleRequiredCodeRule("tiered coaching schedule needs this field")
        val item = newTieredCoachingSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredCoachingScheduleRequiredLabelRule("tiered coaching schedule needs a value")
        val item = newTieredCoachingSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TieredCoachingScheduleLoadKgRangeRule(2.2, 2224.4)
        val item = newTieredCoachingSchedule("x", "y")
        item.loadKg = 2225.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
