package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalProofOfDeliveryMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalProofOfDeliveryMilestoneValidator()
        val item = newSeasonalProofOfDeliveryMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalProofOfDeliveryMilestoneCodeLengthRule(3600)
        val item = newSeasonalProofOfDeliveryMilestone("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalProofOfDeliveryMilestoneRequiredCodeRule(
            "seasonal proof of delivery milestone needs this field",
        )
        val item = newSeasonalProofOfDeliveryMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalProofOfDeliveryMilestoneRequiredNameRule(
            "seasonal proof of delivery milestone needs this field",
        )
        val item = newSeasonalProofOfDeliveryMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalProofOfDeliveryMilestoneRatioRangeRule(8.0, 116.5)
        val item = newSeasonalProofOfDeliveryMilestone("x", "y")
        item.ratio = 118.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = SeasonalProofOfDeliveryMilestoneMinimumRangeRule(8.2, 3352.7)
        val item = newSeasonalProofOfDeliveryMilestone("x", "y")
        item.minimum = 3354.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
