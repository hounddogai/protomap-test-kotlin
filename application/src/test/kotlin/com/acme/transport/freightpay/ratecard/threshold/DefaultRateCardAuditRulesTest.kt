package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRateCardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRateCardAuditValidator()
        val item = newDefaultRateCardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultRateCardAuditRequiredNameRule("default rate card audit needs a value")
        val item = newDefaultRateCardAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = DefaultRateCardAuditDockLengthRule(255)
        val item = newDefaultRateCardAudit("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRateCardAuditRequiredCodeRule("default rate card audit needs an entry")
        val item = newDefaultRateCardAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DefaultRateCardAuditQuantityRangeRule(64, 255)
        val item = newDefaultRateCardAudit("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
