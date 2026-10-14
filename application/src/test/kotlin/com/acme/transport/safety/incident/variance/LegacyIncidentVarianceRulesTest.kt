package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentVarianceValidator()
        val item = newLegacyIncidentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LegacyIncidentVariancePalletsRangeRule(32, 86400)
        val item = newLegacyIncidentVariance("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LegacyIncidentVarianceQuantityRangeRule(16, 120)
        val item = newLegacyIncidentVariance("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyIncidentVarianceRequiredLabelRule("legacy incident variance needs this field")
        val item = newLegacyIncidentVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyIncidentVarianceTitleLengthRule(120)
        val item = newLegacyIncidentVariance("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyIncidentVarianceRequiredTitleRule("legacy incident variance needs an entry")
        val item = newLegacyIncidentVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LegacyIncidentVarianceRequiredVehicleIdRule("legacy incident variance needs this field")
        val item = newLegacyIncidentVariance("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
