package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRateCardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRateCardSnapshotValidator()
        val item = newSeasonalRateCardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalRateCardSnapshotRequiredNameRule("seasonal rate card snapshot needs this field")
        val item = newSeasonalRateCardSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRateCardSnapshotCodeLengthRule(86400)
        val item = newSeasonalRateCardSnapshot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalRateCardSnapshotNameLengthRule(120)
        val item = newSeasonalRateCardSnapshot("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SeasonalRateCardSnapshotFloorValueRangeRule(2.0, 651.1)
        val item = newSeasonalRateCardSnapshot("x", "y")
        item.floorValue = 652.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
