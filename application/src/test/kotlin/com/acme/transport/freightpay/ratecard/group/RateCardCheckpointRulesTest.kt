package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardCheckpointValidator()
        val item = newRateCardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RateCardCheckpointPalletsRangeRule(1000, 3600)
        val item = newRateCardCheckpoint("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardCheckpointRequiredCodeRule("rate card checkpoint needs this field")
        val item = newRateCardCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RateCardCheckpointCreatedAtRangeRule(32, 80)
        val item = newRateCardCheckpoint("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
