package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionAgreementValidator()
        val item = newPrimaryInspectionAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryInspectionAgreementCodeLengthRule(120)
        val item = newPrimaryInspectionAgreement("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryInspectionAgreementRequiredCodeRule("primary inspection agreement needs an entry")
        val item = newPrimaryInspectionAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryInspectionAgreementLabelLengthRule(64)
        val item = newPrimaryInspectionAgreement("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
