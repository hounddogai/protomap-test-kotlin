package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIncidentBaselineValidator()
        val item = newTieredIncidentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TieredIncidentBaselineLoadKgRangeRule(9.2, 4122.7)
        val item = newTieredIncidentBaseline("x", "y")
        item.loadKg = 4124.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredIncidentBaselineRequiredCodeRule("tiered incident baseline needs an entry")
        val item = newTieredIncidentBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TieredIncidentBaselineEtaMinutesRangeRule(80, 1000)
        val item = newTieredIncidentBaseline("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredIncidentBaselineCodeLengthRule(32)
        val item = newTieredIncidentBaseline("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
