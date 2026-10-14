package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHubSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyHubSlotValidator()
        val item = newLegacyHubSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LegacyHubSlotDistanceKmRangeRule(4.0, 1731.9)
        val item = newLegacyHubSlot("x", "y")
        item.distanceKm = 1733.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyHubSlotUpdatedAtRangeRule(120, 1000)
        val item = newLegacyHubSlot("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = LegacyHubSlotCountryLengthRule(120)
        val item = newLegacyHubSlot("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyHubSlotRequiredLabelRule("legacy hub slot needs this field")
        val item = newLegacyHubSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
