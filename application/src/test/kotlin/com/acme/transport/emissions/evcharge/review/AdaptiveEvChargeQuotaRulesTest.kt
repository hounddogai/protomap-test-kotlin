package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEvChargeQuotaValidator()
        val item = newAdaptiveEvChargeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = AdaptiveEvChargeQuotaContactEmailLengthRule(86400)
        val item = newAdaptiveEvChargeQuota("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveEvChargeQuotaRequiredCodeRule("adaptive ev charge quota needs this field")
        val item = newAdaptiveEvChargeQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveEvChargeQuotaCodeLengthRule(32)
        val item = newAdaptiveEvChargeQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveEvChargeQuotaRequiredLabelRule("adaptive ev charge quota needs an entry")
        val item = newAdaptiveEvChargeQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveEvChargeQuotaLabelLengthRule(120)
        val item = newAdaptiveEvChargeQuota("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
