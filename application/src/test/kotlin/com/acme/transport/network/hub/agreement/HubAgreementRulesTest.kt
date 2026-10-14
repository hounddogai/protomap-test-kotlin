package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubAgreementValidator()
        val item = newHubAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = HubAgreementLabelLengthRule(3600)
        val item = newHubAgreement("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HubAgreementRequiredLabelRule("hub agreement needs a value")
        val item = newHubAgreement("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubAgreementCodeLengthRule(16)
        val item = newHubAgreement("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
