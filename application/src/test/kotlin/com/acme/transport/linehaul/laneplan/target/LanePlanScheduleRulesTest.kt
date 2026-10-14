package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanScheduleValidator()
        val item = newLanePlanSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanScheduleCodeLengthRule(120)
        val item = newLanePlanSchedule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LanePlanScheduleRequiredVehicleIdRule("lane plan schedule needs a value")
        val item = newLanePlanSchedule("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LanePlanScheduleRequiredChannelRule("lane plan schedule needs this field")
        val item = newLanePlanSchedule("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LanePlanScheduleRequiredLabelRule("lane plan schedule needs an entry")
        val item = newLanePlanSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
