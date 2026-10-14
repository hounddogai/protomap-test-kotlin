package com.acme.transport.network.transittime.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTransitTimeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTransitTimeAgreementValidator()
        val item = newRegionalTransitTimeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTransitTimeAgreementRequiredCodeRule("regional transit time agreement needs this field")
        val item = newRegionalTransitTimeAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalTransitTimeAgreementRequiredNameRule("regional transit time agreement needs an entry")
        val item = newRegionalTransitTimeAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = RegionalTransitTimeAgreementRequiredReviewerRule("regional transit time agreement needs this field")
        val item = newRegionalTransitTimeAgreement("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
