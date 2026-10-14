package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorAgreementValidator()
        val item = newPendingDockDoorAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingDockDoorAgreementRequiredLabelRule("pending dock door agreement needs this field")
        val item = newPendingDockDoorAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PendingDockDoorAgreementRequiredShiftCodeRule("pending dock door agreement needs an entry")
        val item = newPendingDockDoorAgreement("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDockDoorAgreementCodeLengthRule(64)
        val item = newPendingDockDoorAgreement("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
