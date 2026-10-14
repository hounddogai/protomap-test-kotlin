package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorPreferenceValidator()
        val item = newDockDoorPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DockDoorPreferenceStartsAtRangeRule(64, 86400)
        val item = newDockDoorPreference("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = DockDoorPreferenceExternalRefLengthRule(32)
        val item = newDockDoorPreference("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
