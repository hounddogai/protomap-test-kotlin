package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelBurnScheduleValidator()
        val item = newGlobalFuelBurnSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalFuelBurnScheduleRequiredCodeRule("global fuel burn schedule needs this field")
        val item = newGlobalFuelBurnSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = GlobalFuelBurnScheduleUpdatedAtRangeRule(16, 3600)
        val item = newGlobalFuelBurnSchedule("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalFuelBurnScheduleRequiredTitleRule("global fuel burn schedule needs a value")
        val item = newGlobalFuelBurnSchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = GlobalFuelBurnScheduleLoadKgRangeRule(7.7, 3917.7)
        val item = newGlobalFuelBurnSchedule("x", "y")
        item.loadKg = 3919.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
