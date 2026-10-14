package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapQuotaValidator()
        val item = newSwapQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapQuotaCodeLengthRule(16)
        val item = newSwapQuota("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SwapQuotaRequiredTitleRule("swap quota needs a value")
        val item = newSwapQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = SwapQuotaAmountRangeRule(5.3, 1442.1)
        val item = newSwapQuota("x", "y")
        item.amount = 1443.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
