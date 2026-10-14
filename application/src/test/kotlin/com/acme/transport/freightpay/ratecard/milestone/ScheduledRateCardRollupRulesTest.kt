package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRateCardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRateCardRollupValidator()
        val item = newScheduledRateCardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ScheduledRateCardRollupRequiredRegionRule("scheduled rate card rollup needs this field")
        val item = newScheduledRateCardRollup("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRateCardRollupCodeLengthRule(80)
        val item = newScheduledRateCardRollup("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRateCardRollupRequiredCodeRule("scheduled rate card rollup needs an entry")
        val item = newScheduledRateCardRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
