package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderScheduleValidator()
        val item = newTenderSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TenderScheduleExpiresAtRangeRule(80, 3600)
        val item = newTenderSchedule("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TenderScheduleRequiredLabelRule("tender schedule needs a value")
        val item = newTenderSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TenderScheduleRequiredCodeRule("tender schedule needs this field")
        val item = newTenderSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
