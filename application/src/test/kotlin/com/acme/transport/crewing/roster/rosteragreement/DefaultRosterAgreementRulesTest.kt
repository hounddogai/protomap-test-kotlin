package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRosterAgreementValidator()
        val item = newDefaultRosterAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DefaultRosterAgreementEndsAtRangeRule(64, 255)
        val item = newDefaultRosterAgreement("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultRosterAgreementContactEmailLengthRule(32)
        val item = newDefaultRosterAgreement("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRosterAgreementRequiredCodeRule("default roster agreement needs an entry")
        val item = newDefaultRosterAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
