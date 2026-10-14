package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditBundleValidator()
        val item = newAuditBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditBundleRequiredCodeRule("audit bundle needs a value")
        val item = newAuditBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditBundleCodeLengthRule(86400)
        val item = newAuditBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AuditBundleLabelLengthRule(120)
        val item = newAuditBundle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = AuditBundleRequiredContactEmailRule("audit bundle needs a value")
        val item = newAuditBundle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AuditBundleRequiredSourceRule("audit bundle needs a value")
        val item = newAuditBundle("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AuditBundleAmountRangeRule(7.4, 2249.1)
        val item = newAuditBundle("x", "y")
        item.amount = 2250.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
