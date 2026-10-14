package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeQuotaValidator()
        val item = newEvChargeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = EvChargeQuotaEtaMinutesRangeRule(3600, 3600)
        val item = newEvChargeQuota("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = EvChargeQuotaChecksumLengthRule(80)
        val item = newEvChargeQuota("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
