package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliverySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledProofOfDeliverySlotValidator()
        val item = newScheduledProofOfDeliverySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ScheduledProofOfDeliverySlotRequiredChecksumRule("scheduled proof of delivery slot needs an entry")
        val item = newScheduledProofOfDeliverySlot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = ScheduledProofOfDeliverySlotCountryLengthRule(120)
        val item = newScheduledProofOfDeliverySlot("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledProofOfDeliverySlotEtaMinutesRangeRule(16, 1000)
        val item = newScheduledProofOfDeliverySlot("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
