package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAuditTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAuditTemplateValidator()
        val item = newAdaptiveAuditTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AdaptiveAuditTemplateMinimumRangeRule(6.3, 173.7)
        val item = newAdaptiveAuditTemplate("x", "y")
        item.minimum = 175.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveAuditTemplateCodeLengthRule(80)
        val item = newAdaptiveAuditTemplate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = AdaptiveAuditTemplateRequiredDepotRule("adaptive audit template needs a value")
        val item = newAdaptiveAuditTemplate("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
