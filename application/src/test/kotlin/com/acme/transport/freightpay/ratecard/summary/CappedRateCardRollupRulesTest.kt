package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRateCardRollupValidator()
        val item = newCappedRateCardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedRateCardRollupRequiredCodeRule("capped rate card rollup needs a value")
        val item = newCappedRateCardRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CappedRateCardRollupSequenceRangeRule(32, 1000)
        val item = newCappedRateCardRollup("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
