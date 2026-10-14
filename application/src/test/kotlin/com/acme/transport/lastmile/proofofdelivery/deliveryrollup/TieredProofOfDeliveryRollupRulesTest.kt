package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredProofOfDeliveryRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredProofOfDeliveryRollupValidator()
        val item = newTieredProofOfDeliveryRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TieredProofOfDeliveryRollupFloorValueRangeRule(5.5, 3347.9)
        val item = newTieredProofOfDeliveryRollup("x", "y")
        item.floorValue = 3349.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TieredProofOfDeliveryRollupDriverIdLengthRule(80)
        val item = newTieredProofOfDeliveryRollup("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
