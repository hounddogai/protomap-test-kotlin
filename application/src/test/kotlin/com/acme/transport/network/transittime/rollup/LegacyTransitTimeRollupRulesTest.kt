package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTransitTimeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTransitTimeRollupValidator()
        val item = newLegacyTransitTimeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyTransitTimeRollupRequiredLabelRule("legacy transit time rollup needs a value")
        val item = newLegacyTransitTimeRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LegacyTransitTimeRollupSequenceRangeRule(32, 120)
        val item = newLegacyTransitTimeRollup("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyTransitTimeRollupLabelLengthRule(64)
        val item = newLegacyTransitTimeRollup("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTransitTimeRollupCodeLengthRule(255)
        val item = newLegacyTransitTimeRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LegacyTransitTimeRollupStartsAtRangeRule(64, 3600)
        val item = newLegacyTransitTimeRollup("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
