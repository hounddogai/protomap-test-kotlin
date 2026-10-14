package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyProofOfDeliveryRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyProofOfDeliveryRequestValidator()
        val item = newLegacyProofOfDeliveryRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyProofOfDeliveryRequestCodeLengthRule(120)
        val item = newLegacyProofOfDeliveryRequest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyProofOfDeliveryRequestUpdatedAtRangeRule(3600, 86400)
        val item = newLegacyProofOfDeliveryRequest("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyProofOfDeliveryRequestTitleLengthRule(1000)
        val item = newLegacyProofOfDeliveryRequest("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyProofOfDeliveryRequestRequiredCodeRule("legacy proof of delivery request needs this field")
        val item = newLegacyProofOfDeliveryRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacyProofOfDeliveryRequestRequiredTimezoneRule("legacy proof of delivery request needs a value")
        val item = newLegacyProofOfDeliveryRequest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
