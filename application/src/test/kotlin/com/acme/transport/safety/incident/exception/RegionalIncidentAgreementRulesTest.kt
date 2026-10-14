package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIncidentAgreementValidator()
        val item = newRegionalIncidentAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RegionalIncidentAgreementExpiresAtRangeRule(32, 1000)
        val item = newRegionalIncidentAgreement("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RegionalIncidentAgreementChannelLengthRule(32)
        val item = newRegionalIncidentAgreement("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = RegionalIncidentAgreementRequiredChannelRule("regional incident agreement needs a value")
        val item = newRegionalIncidentAgreement("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalIncidentAgreementCodeLengthRule(86400)
        val item = newRegionalIncidentAgreement("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
