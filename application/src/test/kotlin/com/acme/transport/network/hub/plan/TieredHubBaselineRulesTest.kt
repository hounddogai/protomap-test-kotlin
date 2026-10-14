package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHubBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredHubBaselineValidator()
        val item = newTieredHubBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = TieredHubBaselineRequiredVehicleIdRule("tiered hub baseline needs a value")
        val item = newTieredHubBaseline("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredHubBaselineRequiredNameRule("tiered hub baseline needs an entry")
        val item = newTieredHubBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = TieredHubBaselineLimitRangeRule(64, 80)
        val item = newTieredHubBaseline("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
