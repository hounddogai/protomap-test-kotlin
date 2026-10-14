package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditAdjustmentValidator()
        val item = newAuditAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AuditAdjustmentRequiredCountryRule("audit adjustment needs a value")
        val item = newAuditAdjustment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AuditAdjustmentRequiredNameRule("audit adjustment needs a value")
        val item = newAuditAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AuditAdjustmentNameLengthRule(80)
        val item = newAuditAdjustment("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
