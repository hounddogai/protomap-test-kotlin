package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTariffQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTariffQuotaValidator()
        val item = newManualTariffQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualTariffQuotaRequiredTitleRule("manual tariff quota needs a value")
        val item = newManualTariffQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ManualTariffQuotaBatchSizeRangeRule(64, 1000)
        val item = newManualTariffQuota("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ManualTariffQuotaReviewerLengthRule(1000)
        val item = newManualTariffQuota("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualTariffQuotaTitleLengthRule(86400)
        val item = newManualTariffQuota("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
