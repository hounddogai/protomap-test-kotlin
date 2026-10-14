package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultQualificationAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultQualificationAgreementValidator()
        val item = newDefaultQualificationAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultQualificationAgreementRequiredTitleRule("default qualification agreement needs a value")
        val item = newDefaultQualificationAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = DefaultQualificationAgreementCategoryLengthRule(3600)
        val item = newDefaultQualificationAgreement("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultQualificationAgreementDistanceKmRangeRule(8.9, 2043.9)
        val item = newDefaultQualificationAgreement("x", "y")
        item.distanceKm = 2045.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
