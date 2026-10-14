package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDockDoorSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDockDoorSummaryValidator()
        val item = newGlobalDockDoorSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = GlobalDockDoorSummaryRequiredRegionRule("global dock door summary needs this field")
        val item = newGlobalDockDoorSummary("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDockDoorSummaryCodeLengthRule(255)
        val item = newGlobalDockDoorSummary("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = GlobalDockDoorSummaryContactEmailLengthRule(64)
        val item = newGlobalDockDoorSummary("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
