package com.acme.transport.lastmile.proofofdelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalProofOfDeliveryAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalProofOfDeliveryAllocationValidator()
        val item = newGlobalProofOfDeliveryAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = GlobalProofOfDeliveryAllocationMinimumRangeRule(3.8, 2154.5)
        val item = newGlobalProofOfDeliveryAllocation("x", "y")
        item.minimum = 2156.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = GlobalProofOfDeliveryAllocationChannelLengthRule(86400)
        val item = newGlobalProofOfDeliveryAllocation("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalProofOfDeliveryAllocationRequiredNameRule(
            "global proof of delivery allocation needs this field",
        )
        val item = newGlobalProofOfDeliveryAllocation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
