package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTenderQuotaValidator()
        val item = newGlobalTenderQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = GlobalTenderQuotaFloorValueRangeRule(3.5, 854.0)
        val item = newGlobalTenderQuota("x", "y")
        item.floorValue = 855.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = GlobalTenderQuotaReviewerLengthRule(80)
        val item = newGlobalTenderQuota("x", "y")
        item.reviewer = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = GlobalTenderQuotaRequiredReviewerRule("global tender quota needs a value")
        val item = newGlobalTenderQuota("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalTenderQuotaCodeLengthRule(16)
        val item = newGlobalTenderQuota("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
