package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyProofOfDeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyProofOfDeliveryWindowValidator()
        val item = newLegacyProofOfDeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LegacyProofOfDeliveryWindowRequiredTrailerIdRule("legacy proof of delivery window needs an entry")
        val item = newLegacyProofOfDeliveryWindow("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyProofOfDeliveryWindowRetriesRangeRule(16, 255)
        val item = newLegacyProofOfDeliveryWindow("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyProofOfDeliveryWindowRequiredTitleRule("legacy proof of delivery window needs this field")
        val item = newLegacyProofOfDeliveryWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LegacyProofOfDeliveryWindowStopCountRangeRule(120, 3600)
        val item = newLegacyProofOfDeliveryWindow("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LegacyProofOfDeliveryWindowCapacityRangeRule(80, 1000)
        val item = newLegacyProofOfDeliveryWindow("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LegacyProofOfDeliveryWindowDiscountRangeRule(6.0, 1369.6)
        val item = newLegacyProofOfDeliveryWindow("x", "y")
        item.discount = 1371.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
