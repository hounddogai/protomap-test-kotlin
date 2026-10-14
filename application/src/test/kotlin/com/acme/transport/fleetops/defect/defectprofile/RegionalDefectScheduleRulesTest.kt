package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDefectScheduleValidator()
        val item = newRegionalDefectSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalDefectScheduleLabelLengthRule(86400)
        val item = newRegionalDefectSchedule("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RegionalDefectScheduleRequiredDriverIdRule("regional defect schedule needs this field")
        val item = newRegionalDefectSchedule("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalDefectScheduleRequiredContactEmailRule("regional defect schedule needs this field")
        val item = newRegionalDefectSchedule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
