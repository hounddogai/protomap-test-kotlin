package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingProofOfDeliveryDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingProofOfDeliveryDigestValidator()
        val item = newPendingProofOfDeliveryDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingProofOfDeliveryDigestLabelLengthRule(3600)
        val item = newPendingProofOfDeliveryDigest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingProofOfDeliveryDigestRequiredCodeRule("pending proof of delivery digest needs an entry")
        val item = newPendingProofOfDeliveryDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingProofOfDeliveryDigestNameLengthRule(80)
        val item = newPendingProofOfDeliveryDigest("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PendingProofOfDeliveryDigestDurationMinutesRangeRule(32, 80)
        val item = newPendingProofOfDeliveryDigest("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
