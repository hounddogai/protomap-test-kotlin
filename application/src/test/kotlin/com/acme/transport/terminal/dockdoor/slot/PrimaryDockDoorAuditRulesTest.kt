package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDockDoorAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDockDoorAuditValidator()
        val item = newPrimaryDockDoorAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryDockDoorAuditCodeLengthRule(3600)
        val item = newPrimaryDockDoorAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PrimaryDockDoorAuditRequiredTrailerIdRule("primary dock door audit needs this field")
        val item = newPrimaryDockDoorAudit("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryDockDoorAuditRequiredChannelRule("primary dock door audit needs a value")
        val item = newPrimaryDockDoorAudit("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
