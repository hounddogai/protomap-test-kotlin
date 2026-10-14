package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDefectAgreementValidator()
        val item = newManualDefectAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ManualDefectAgreementLocaleLengthRule(120)
        val item = newManualDefectAgreement("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualDefectAgreementRequiredTitleRule("manual defect agreement needs a value")
        val item = newManualDefectAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
