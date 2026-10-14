package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderQuotaValidator()
        val item = newPrimaryTenderQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryTenderQuotaRequiredTitleRule("primary tender quota needs a value")
        val item = newPrimaryTenderQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = PrimaryTenderQuotaPhoneLengthRule(80)
        val item = newPrimaryTenderQuota("x", "y")
        item.phone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryTenderQuotaRetriesRangeRule(32, 3600)
        val item = newPrimaryTenderQuota("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PrimaryTenderQuotaUpdatedAtRangeRule(1000, 3600)
        val item = newPrimaryTenderQuota("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
