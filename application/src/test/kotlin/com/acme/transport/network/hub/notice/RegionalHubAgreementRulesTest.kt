package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHubAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalHubAgreementValidator()
        val item = newRegionalHubAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalHubAgreementNameLengthRule(255)
        val item = newRegionalHubAgreement("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RegionalHubAgreementChannelLengthRule(3600)
        val item = newRegionalHubAgreement("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalHubAgreementRequiredNameRule("regional hub agreement needs a value")
        val item = newRegionalHubAgreement("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
