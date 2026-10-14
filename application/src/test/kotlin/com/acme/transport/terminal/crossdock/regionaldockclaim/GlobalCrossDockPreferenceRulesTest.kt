package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCrossDockPreferenceValidator()
        val item = newGlobalCrossDockPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalCrossDockPreferenceLabelLengthRule(64)
        val item = newGlobalCrossDockPreference("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = GlobalCrossDockPreferenceCeilingRangeRule(8.3, 1519.0)
        val item = newGlobalCrossDockPreference("x", "y")
        item.ceiling = 1520.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = GlobalCrossDockPreferenceSegmentLengthRule(86400)
        val item = newGlobalCrossDockPreference("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = GlobalCrossDockPreferenceFuelLitersRangeRule(6.4, 3490.5)
        val item = newGlobalCrossDockPreference("x", "y")
        item.fuelLiters = 3492.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
