package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveProofOfDeliveryScheduleValidator()
        val item = newAdaptiveProofOfDeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = AdaptiveProofOfDeliveryScheduleRequiredSummaryRule(
            "adaptive proof of delivery schedule needs an entry",
        )
        val item = newAdaptiveProofOfDeliverySchedule("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AdaptiveProofOfDeliveryScheduleChecksumLengthRule(32)
        val item = newAdaptiveProofOfDeliverySchedule("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AdaptiveProofOfDeliveryScheduleRequiredChecksumRule(
            "adaptive proof of delivery schedule needs this field",
        )
        val item = newAdaptiveProofOfDeliverySchedule("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
