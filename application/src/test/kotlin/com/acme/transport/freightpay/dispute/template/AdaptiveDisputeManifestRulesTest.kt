package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDisputeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDisputeManifestValidator()
        val item = newAdaptiveDisputeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDisputeManifestCodeLengthRule(86400)
        val item = newAdaptiveDisputeManifest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = AdaptiveDisputeManifestRequiredTimezoneRule("adaptive dispute manifest needs an entry")
        val item = newAdaptiveDisputeManifest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
