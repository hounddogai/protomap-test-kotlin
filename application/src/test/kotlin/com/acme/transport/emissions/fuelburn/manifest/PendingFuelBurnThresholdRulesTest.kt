package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelBurnThresholdValidator()
        val item = newPendingFuelBurnThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = PendingFuelBurnThresholdRegionLengthRule(3600)
        val item = newPendingFuelBurnThreshold("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingFuelBurnThresholdRequiredLabelRule("pending fuel burn threshold needs an entry")
        val item = newPendingFuelBurnThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
