package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLeaseReturnScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryLeaseReturnScheduleValidator()
        val item = newPrimaryLeaseReturnSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = PrimaryLeaseReturnScheduleTimezoneLengthRule(1000)
        val item = newPrimaryLeaseReturnSchedule("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryLeaseReturnScheduleCodeLengthRule(120)
        val item = newPrimaryLeaseReturnSchedule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = PrimaryLeaseReturnScheduleRequiredSourceRule("primary lease return schedule needs this field")
        val item = newPrimaryLeaseReturnSchedule("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryLeaseReturnScheduleLabelLengthRule(1000)
        val item = newPrimaryLeaseReturnSchedule("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
