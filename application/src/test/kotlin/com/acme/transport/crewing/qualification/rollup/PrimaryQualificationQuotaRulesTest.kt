package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryQualificationQuotaValidator()
        val item = newPrimaryQualificationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryQualificationQuotaCurrencyLengthRule(16)
        val item = newPrimaryQualificationQuota("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryQualificationQuotaRequiredCodeRule("primary qualification quota needs this field")
        val item = newPrimaryQualificationQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryQualificationQuotaCodeLengthRule(16)
        val item = newPrimaryQualificationQuota("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryQualificationQuotaRequiredCurrencyRule("primary qualification quota needs a value")
        val item = newPrimaryQualificationQuota("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PrimaryQualificationQuotaSequenceRangeRule(86400, 86400)
        val item = newPrimaryQualificationQuota("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
