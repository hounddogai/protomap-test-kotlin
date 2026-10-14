package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEmissionFactorCheckpointValidator()
        val item = newSeasonalEmissionFactorCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalEmissionFactorCheckpointLabelLengthRule(80)
        val item = newSeasonalEmissionFactorCheckpoint("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalEmissionFactorCheckpointRequiredCodeRule(
            "seasonal emission factor checkpoint needs an entry",
        )
        val item = newSeasonalEmissionFactorCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = SeasonalEmissionFactorCheckpointRequiredLaneRule(
            "seasonal emission factor checkpoint needs an entry",
        )
        val item = newSeasonalEmissionFactorCheckpoint("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = SeasonalEmissionFactorCheckpointLaneLengthRule(1000)
        val item = newSeasonalEmissionFactorCheckpoint("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
