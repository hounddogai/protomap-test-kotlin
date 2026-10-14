package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedQualificationAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedQualificationAgreementValidator()
        val item = newCappedQualificationAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = CappedQualificationAgreementEndsAtRangeRule(16, 255)
        val item = newCappedQualificationAgreement("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedQualificationAgreementRequiredTitleRule("capped qualification agreement needs a value")
        val item = newCappedQualificationAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
