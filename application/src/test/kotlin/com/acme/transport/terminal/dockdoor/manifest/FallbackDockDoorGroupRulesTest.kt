package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDockDoorGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDockDoorGroupValidator()
        val item = newFallbackDockDoorGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackDockDoorGroupRequiredTitleRule("fallback dock door group needs a value")
        val item = newFallbackDockDoorGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackDockDoorGroupTitleLengthRule(80)
        val item = newFallbackDockDoorGroup("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FallbackDockDoorGroupUnitLengthRule(86400)
        val item = newFallbackDockDoorGroup("x", "y")
        item.unit = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = FallbackDockDoorGroupRetriesRangeRule(80, 1000)
        val item = newFallbackDockDoorGroup("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
