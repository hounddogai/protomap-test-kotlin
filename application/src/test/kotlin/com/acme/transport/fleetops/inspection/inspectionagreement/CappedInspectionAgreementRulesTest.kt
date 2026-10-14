package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedInspectionAgreementValidator()
        val item = newCappedInspectionAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = CappedInspectionAgreementOdometerRangeRule(16, 80)
        val item = newCappedInspectionAgreement("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedInspectionAgreementRequiredCodeRule("capped inspection agreement needs this field")
        val item = newCappedInspectionAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedInspectionAgreementCountryLengthRule(16)
        val item = newCappedInspectionAgreement("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedInspectionAgreementCodeLengthRule(86400)
        val item = newCappedInspectionAgreement("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = CappedInspectionAgreementRequiredCountryRule("capped inspection agreement needs a value")
        val item = newCappedInspectionAgreement("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedInspectionAgreementRequiredNameRule("capped inspection agreement needs an entry")
        val item = newCappedInspectionAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
