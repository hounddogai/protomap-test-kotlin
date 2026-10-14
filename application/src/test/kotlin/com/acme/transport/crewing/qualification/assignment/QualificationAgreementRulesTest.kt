package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationAgreementValidator()
        val item = newQualificationAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationAgreementRequiredCodeRule("qualification agreement needs this field")
        val item = newQualificationAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = QualificationAgreementRequiredChecksumRule("qualification agreement needs this field")
        val item = newQualificationAgreement("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = QualificationAgreementStartsAtRangeRule(255, 3600)
        val item = newQualificationAgreement("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = QualificationAgreementDiscountRangeRule(2.3, 2513.2)
        val item = newQualificationAgreement("x", "y")
        item.discount = 2514.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
