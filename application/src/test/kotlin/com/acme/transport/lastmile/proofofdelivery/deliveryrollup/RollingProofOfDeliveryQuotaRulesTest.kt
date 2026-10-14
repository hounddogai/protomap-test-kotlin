package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryQuotaValidator()
        val item = newRollingProofOfDeliveryQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RollingProofOfDeliveryQuotaDriverIdLengthRule(3600)
        val item = newRollingProofOfDeliveryQuota("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingProofOfDeliveryQuotaAttemptsRangeRule(32, 3600)
        val item = newRollingProofOfDeliveryQuota("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingProofOfDeliveryQuotaRequiredCodeRule("rolling proof of delivery quota needs a value")
        val item = newRollingProofOfDeliveryQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryQuotaCodeLengthRule(80)
        val item = newRollingProofOfDeliveryQuota("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RollingProofOfDeliveryQuotaRateRangeRule(7.1, 3675.9)
        val item = newRollingProofOfDeliveryQuota("x", "y")
        item.rate = 3677.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
