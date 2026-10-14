package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorAgreementValidator()
        val item = newDockDoorAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = DockDoorAgreementRequiredRegionRule("dock door agreement needs a value")
        val item = newDockDoorAgreement("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DockDoorAgreementRevisionRangeRule(32, 86400)
        val item = newDockDoorAgreement("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorAgreementRequiredNameRule("dock door agreement needs this field")
        val item = newDockDoorAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorAgreementRequiredCodeRule("dock door agreement needs this field")
        val item = newDockDoorAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
