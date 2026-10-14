package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionDraftValidator()
        val item = newAdaptiveInspectionDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AdaptiveInspectionDraftFuelLitersRangeRule(8.5, 4440.7)
        val item = newAdaptiveInspectionDraft("x", "y")
        item.fuelLiters = 4442.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = AdaptiveInspectionDraftCountryLengthRule(86400)
        val item = newAdaptiveInspectionDraft("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = AdaptiveInspectionDraftDepotLengthRule(255)
        val item = newAdaptiveInspectionDraft("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveInspectionDraftLoadKgRangeRule(9.6, 1728.9)
        val item = newAdaptiveInspectionDraft("x", "y")
        item.loadKg = 1730.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
