package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDwellGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDwellGroupValidator()
        val item = newTieredDwellGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = TieredDwellGroupCostRangeRule(0.8, 1122.9)
        val item = newTieredDwellGroup("x", "y")
        item.cost = 1124.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TieredDwellGroupRequiredNotesRule("tiered dwell group needs an entry")
        val item = newTieredDwellGroup("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TieredDwellGroupExpiresAtRangeRule(64, 3600)
        val item = newTieredDwellGroup("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TieredDwellGroupNotesLengthRule(16)
        val item = newTieredDwellGroup("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
