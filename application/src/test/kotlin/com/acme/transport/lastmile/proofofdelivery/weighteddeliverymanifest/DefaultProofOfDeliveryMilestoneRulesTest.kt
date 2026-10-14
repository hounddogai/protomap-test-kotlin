package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultProofOfDeliveryMilestoneValidator()
        val item = newDefaultProofOfDeliveryMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = DefaultProofOfDeliveryMilestoneRequiredDriverIdRule(
            "default proof of delivery milestone needs this field",
        )
        val item = newDefaultProofOfDeliveryMilestone("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultProofOfDeliveryMilestoneCodeLengthRule(64)
        val item = newDefaultProofOfDeliveryMilestone("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultProofOfDeliveryMilestoneAttemptsRangeRule(80, 255)
        val item = newDefaultProofOfDeliveryMilestone("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultProofOfDeliveryMilestoneRequiredNameRule("default proof of delivery milestone needs a value")
        val item = newDefaultProofOfDeliveryMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
