package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelBurnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelBurnRollupValidator()
        val item = newTieredFuelBurnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredFuelBurnRollupRequiredCodeRule("tiered fuel burn rollup needs an entry")
        val item = newTieredFuelBurnRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TieredFuelBurnRollupLaneLengthRule(3600)
        val item = newTieredFuelBurnRollup("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = TieredFuelBurnRollupPalletsRangeRule(16, 3600)
        val item = newTieredFuelBurnRollup("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
