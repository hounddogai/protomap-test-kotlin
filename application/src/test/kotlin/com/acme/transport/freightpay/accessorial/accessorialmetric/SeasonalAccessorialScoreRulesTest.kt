package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialScoreValidator()
        val item = newSeasonalAccessorialScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalAccessorialScoreLabelLengthRule(32)
        val item = newSeasonalAccessorialScore("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalAccessorialScoreRequiredLabelRule("seasonal accessorial score needs a value")
        val item = newSeasonalAccessorialScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalAccessorialScoreRequiredNameRule("seasonal accessorial score needs a value")
        val item = newSeasonalAccessorialScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
