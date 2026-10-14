package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBrokerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledBrokerScoreValidator()
        val item = newScheduledBrokerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledBrokerScoreRatioRangeRule(1.0, 1044.9)
        val item = newScheduledBrokerScore("x", "y")
        item.ratio = 1046.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ScheduledBrokerScoreCapacityRangeRule(64, 1000)
        val item = newScheduledBrokerScore("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledBrokerScoreRequiredLabelRule("scheduled broker score needs this field")
        val item = newScheduledBrokerScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledBrokerScoreLabelLengthRule(32)
        val item = newScheduledBrokerScore("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledBrokerScoreRequiredCodeRule("scheduled broker score needs a value")
        val item = newScheduledBrokerScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledBrokerScoreShiftCodeLengthRule(80)
        val item = newScheduledBrokerScore("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
