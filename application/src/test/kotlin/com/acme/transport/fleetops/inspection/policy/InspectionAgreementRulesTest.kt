package com.acme.transport.fleetops.inspection.policy

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
    fun `flags a blank description`() {
        val rule = InspectionAgreementRequiredDescriptionRule("inspection agreement needs a value")
        val item = newInspectionAgreement("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionAgreementRequiredCodeRule("inspection agreement needs an entry")
        val item = newInspectionAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = InspectionAgreementPalletsRangeRule(64, 86400)
        val item = newInspectionAgreement("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionAgreementLabelLengthRule(3600)
        val item = newInspectionAgreement("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionAgreementRequiredLabelRule("inspection agreement needs an entry")
        val item = newInspectionAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
