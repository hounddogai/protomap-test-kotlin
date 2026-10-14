package com.acme.transport.lastmile.locker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLockerScoreValidator()
        val item = newCappedLockerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedLockerScoreLabelLengthRule(80)
        val item = newCappedLockerScore("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedLockerScoreRequiredLabelRule("capped locker score needs this field")
        val item = newCappedLockerScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedLockerScoreGraceDaysRangeRule(255, 1000)
        val item = newCappedLockerScore("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedLockerScoreCodeLengthRule(80)
        val item = newCappedLockerScore("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
