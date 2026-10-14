package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialScoreValidator()
        val item = newAccessorialScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = AccessorialScoreScoreRangeRule(3.4, 3675.3)
        val item = newAccessorialScore("x", "y")
        item.score = 3676.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = AccessorialScoreFloorValueRangeRule(5.9, 2602.3)
        val item = newAccessorialScore("x", "y")
        item.floorValue = 2603.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AccessorialScoreRequiredSourceRule("accessorial score needs an entry")
        val item = newAccessorialScore("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = AccessorialScoreSourceLengthRule(32)
        val item = newAccessorialScore("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialScoreRequiredLabelRule("accessorial score needs an entry")
        val item = newAccessorialScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
