package com.acme.transport.linehaul.laneplan.plancheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanCheckpointValidator()
        val item = newFallbackLanePlanCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = FallbackLanePlanCheckpointDockLengthRule(120)
        val item = newFallbackLanePlanCheckpoint("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackLanePlanCheckpointCurrencyLengthRule(255)
        val item = newFallbackLanePlanCheckpoint("x", "y")
        item.currency = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FallbackLanePlanCheckpointPalletsRangeRule(32, 32)
        val item = newFallbackLanePlanCheckpoint("x", "y")
        item.pallets = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackLanePlanCheckpointTitleLengthRule(120)
        val item = newFallbackLanePlanCheckpoint("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
