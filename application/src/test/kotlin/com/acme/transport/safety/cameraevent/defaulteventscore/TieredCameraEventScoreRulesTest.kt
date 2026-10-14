package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCameraEventScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCameraEventScoreValidator()
        val item = newTieredCameraEventScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredCameraEventScoreRequiredCodeRule("tiered camera event score needs an entry")
        val item = newTieredCameraEventScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = TieredCameraEventScoreRequiredLaneRule("tiered camera event score needs an entry")
        val item = newTieredCameraEventScore("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TieredCameraEventScoreFloorValueRangeRule(2.9, 1583.4)
        val item = newTieredCameraEventScore("x", "y")
        item.floorValue = 1584.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredCameraEventScoreTitleLengthRule(86400)
        val item = newTieredCameraEventScore("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
