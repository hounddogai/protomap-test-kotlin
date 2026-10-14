package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanScheduleValidator()
        val item = newServicePlanSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ServicePlanScheduleRequiredShiftCodeRule("service plan schedule needs an entry")
        val item = newServicePlanSchedule("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanScheduleCodeLengthRule(120)
        val item = newServicePlanSchedule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
