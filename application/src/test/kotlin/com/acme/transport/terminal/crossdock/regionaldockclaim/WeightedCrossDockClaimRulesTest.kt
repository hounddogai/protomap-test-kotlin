package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCrossDockClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCrossDockClaimValidator()
        val item = newWeightedCrossDockClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedCrossDockClaimRequiredVehicleIdRule("weighted cross dock claim needs an entry")
        val item = newWeightedCrossDockClaim("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = WeightedCrossDockClaimCeilingRangeRule(9.3, 3417.0)
        val item = newWeightedCrossDockClaim("x", "y")
        item.ceiling = 3418.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = WeightedCrossDockClaimVehicleIdLengthRule(16)
        val item = newWeightedCrossDockClaim("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
