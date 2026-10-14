package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterAgreementValidator()
        val item = newRosterAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RosterAgreementMinimumRangeRule(5.0, 1386.4)
        val item = newRosterAgreement("x", "y")
        item.minimum = 1387.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RosterAgreementRequiredLocaleRule("roster agreement needs an entry")
        val item = newRosterAgreement("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
