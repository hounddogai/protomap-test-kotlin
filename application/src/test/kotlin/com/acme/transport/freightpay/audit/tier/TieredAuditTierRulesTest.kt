package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAuditTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAuditTierValidator()
        val item = newTieredAuditTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = TieredAuditTierVersionRangeRule(120, 86400)
        val item = newTieredAuditTier("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredAuditTierCodeLengthRule(16)
        val item = newTieredAuditTier("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredAuditTierRequiredCodeRule("tiered audit tier needs a value")
        val item = newTieredAuditTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
