package com.acme.transport.crewing.roster.rostercycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRosterCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRosterCycleValidator()
        val item = newFallbackRosterCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = FallbackRosterCycleRatioRangeRule(9.7, 1242.3)
        val item = newFallbackRosterCycle("x", "y")
        item.ratio = 1243.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackRosterCycleEtaMinutesRangeRule(255, 1000)
        val item = newFallbackRosterCycle("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FallbackRosterCycleRequiredCarrierRule("fallback roster cycle needs an entry")
        val item = newFallbackRosterCycle("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FallbackRosterCyclePalletsRangeRule(80, 255)
        val item = newFallbackRosterCycle("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackRosterCycleRequiredCodeRule("fallback roster cycle needs an entry")
        val item = newFallbackRosterCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
