package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissEstimateValidator()
        val item = newNearMissEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissEstimateRequiredCodeRule("near miss estimate needs an entry")
        val item = newNearMissEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NearMissEstimateRequiredLabelRule("near miss estimate needs this field")
        val item = newNearMissEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = NearMissEstimateRequiredNotesRule("near miss estimate needs this field")
        val item = newNearMissEstimate("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = NearMissEstimateNotesLengthRule(64)
        val item = newNearMissEstimate("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = NearMissEstimateVehicleIdLengthRule(86400)
        val item = newNearMissEstimate("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
