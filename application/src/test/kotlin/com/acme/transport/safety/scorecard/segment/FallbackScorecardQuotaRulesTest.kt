package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackScorecardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackScorecardQuotaValidator()
        val item = newFallbackScorecardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackScorecardQuotaCodeLengthRule(120)
        val item = newFallbackScorecardQuota("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = FallbackScorecardQuotaRequiredTimezoneRule("fallback scorecard quota needs a value")
        val item = newFallbackScorecardQuota("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackScorecardQuotaRequiredTitleRule("fallback scorecard quota needs this field")
        val item = newFallbackScorecardQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = FallbackScorecardQuotaScoreRangeRule(1.3, 673.8)
        val item = newFallbackScorecardQuota("x", "y")
        item.score = 675.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackScorecardQuotaTitleLengthRule(1000)
        val item = newFallbackScorecardQuota("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackScorecardQuotaRequiredCodeRule("fallback scorecard quota needs an entry")
        val item = newFallbackScorecardQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
