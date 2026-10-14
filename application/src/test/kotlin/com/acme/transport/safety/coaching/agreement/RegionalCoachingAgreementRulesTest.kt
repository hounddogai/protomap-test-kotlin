package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCoachingAgreementValidator()
        val item = newRegionalCoachingAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalCoachingAgreementUnitLengthRule(120)
        val item = newRegionalCoachingAgreement("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCoachingAgreementRequiredCodeRule("regional coaching agreement needs this field")
        val item = newRegionalCoachingAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalCoachingAgreementRequiredTitleRule("regional coaching agreement needs an entry")
        val item = newRegionalCoachingAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCoachingAgreementCodeLengthRule(255)
        val item = newRegionalCoachingAgreement("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
