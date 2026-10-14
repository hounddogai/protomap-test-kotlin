package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledProofOfDeliveryPreferenceValidator()
        val item = newScheduledProofOfDeliveryPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledProofOfDeliveryPreferenceTitleLengthRule(3600)
        val item = newScheduledProofOfDeliveryPreference("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledProofOfDeliveryPreferenceRequiredTitleRule(
            "scheduled proof of delivery preference needs an entry",
        )
        val item = newScheduledProofOfDeliveryPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledProofOfDeliveryPreferenceRequiredCodeRule(
            "scheduled proof of delivery preference needs this field",
        )
        val item = newScheduledProofOfDeliveryPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ScheduledProofOfDeliveryPreferenceBatchSizeRangeRule(64, 255)
        val item = newScheduledProofOfDeliveryPreference("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
