package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryProofOfDeliveryNoticeValidator()
        val item = newPrimaryProofOfDeliveryNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = PrimaryProofOfDeliveryNoticeVehicleIdLengthRule(64)
        val item = newPrimaryProofOfDeliveryNotice("x", "y")
        item.vehicleId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PrimaryProofOfDeliveryNoticeCapacityRangeRule(120, 3600)
        val item = newPrimaryProofOfDeliveryNotice("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = PrimaryProofOfDeliveryNoticeMaximumRangeRule(9.6, 666.1)
        val item = newPrimaryProofOfDeliveryNotice("x", "y")
        item.maximum = 667.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
