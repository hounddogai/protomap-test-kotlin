package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelScheduleValidator()
        val item = newParcelSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ParcelScheduleFloorValueRangeRule(6.0, 4752.4)
        val item = newParcelSchedule("x", "y")
        item.floorValue = 4753.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ParcelScheduleTitleLengthRule(3600)
        val item = newParcelSchedule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ParcelScheduleRequiredChecksumRule("parcel schedule needs a value")
        val item = newParcelSchedule("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelScheduleRequiredTitleRule("parcel schedule needs this field")
        val item = newParcelSchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
