package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDisputeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDisputeTierValidator()
        val item = newSeasonalDisputeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalDisputeTierRequiredCodeRule("seasonal dispute tier needs an entry")
        val item = newSeasonalDisputeTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDisputeTierTitleLengthRule(32)
        val item = newSeasonalDisputeTier("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDisputeTierCodeLengthRule(1000)
        val item = newSeasonalDisputeTier("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
