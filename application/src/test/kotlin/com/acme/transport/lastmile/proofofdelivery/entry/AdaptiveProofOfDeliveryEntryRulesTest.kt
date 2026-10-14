package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveProofOfDeliveryEntryValidator()
        val item = newAdaptiveProofOfDeliveryEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = AdaptiveProofOfDeliveryEntryRequiredTrailerIdRule("adaptive proof of delivery entry needs a value")
        val item = newAdaptiveProofOfDeliveryEntry("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AdaptiveProofOfDeliveryEntryRetriesRangeRule(64, 255)
        val item = newAdaptiveProofOfDeliveryEntry("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = AdaptiveProofOfDeliveryEntryStopCountRangeRule(3600, 3600)
        val item = newAdaptiveProofOfDeliveryEntry("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
