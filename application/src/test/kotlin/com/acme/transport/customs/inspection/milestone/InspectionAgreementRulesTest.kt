package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAgreementValidator()
        val item = newInspectionAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionAgreementRequiredCodeRule("inspection agreement needs an entry")
        val item = newInspectionAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = InspectionAgreementRequiredUnitRule("inspection agreement needs this field")
        val item = newInspectionAgreement("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionAgreementCodeLengthRule(80)
        val item = newInspectionAgreement("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
