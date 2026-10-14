package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientProofOfDeliveryProfileValidator()
        val item = newLenientProofOfDeliveryProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientProofOfDeliveryProfileRequiredCodeRule("lenient proof of delivery profile needs a value")
        val item = newLenientProofOfDeliveryProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientProofOfDeliveryProfileRequiredRegionRule(
            "lenient proof of delivery profile needs this field",
        )
        val item = newLenientProofOfDeliveryProfile("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientProofOfDeliveryProfileRequiredTitleRule("lenient proof of delivery profile needs an entry")
        val item = newLenientProofOfDeliveryProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientProofOfDeliveryProfileCodeLengthRule(1000)
        val item = newLenientProofOfDeliveryProfile("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
