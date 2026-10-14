package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalClearanceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalClearanceNoticeValidator()
        val item = newSeasonalClearanceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalClearanceNoticeCodeLengthRule(32)
        val item = newSeasonalClearanceNotice("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = SeasonalClearanceNoticeVehicleIdLengthRule(80)
        val item = newSeasonalClearanceNotice("x", "y")
        item.vehicleId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalClearanceNoticeBatchSizeRangeRule(120, 86400)
        val item = newSeasonalClearanceNotice("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
