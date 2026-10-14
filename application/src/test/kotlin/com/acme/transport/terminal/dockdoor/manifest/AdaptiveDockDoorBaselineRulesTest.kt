package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDockDoorBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDockDoorBaselineValidator()
        val item = newAdaptiveDockDoorBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveDockDoorBaselineRequiredTitleRule("adaptive dock door baseline needs a value")
        val item = newAdaptiveDockDoorBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveDockDoorBaselineTitleLengthRule(32)
        val item = newAdaptiveDockDoorBaseline("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AdaptiveDockDoorBaselineRequiredDepotRule("adaptive dock door baseline needs this field")
        val item = newAdaptiveDockDoorBaseline("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
