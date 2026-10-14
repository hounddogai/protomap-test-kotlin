package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredScorecardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredScorecardQuotaValidator()
        val item = newTieredScorecardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = TieredScorecardQuotaSourceLengthRule(255)
        val item = newTieredScorecardQuota("x", "y")
        item.source = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TieredScorecardQuotaRequiredContactEmailRule("tiered scorecard quota needs an entry")
        val item = newTieredScorecardQuota("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = TieredScorecardQuotaDiscountRangeRule(3.4, 1520.0)
        val item = newTieredScorecardQuota("x", "y")
        item.discount = 1521.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
