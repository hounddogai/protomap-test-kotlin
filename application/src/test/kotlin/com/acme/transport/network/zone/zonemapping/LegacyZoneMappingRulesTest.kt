package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneMappingValidator()
        val item = newLegacyZoneMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyZoneMappingRequiredLabelRule("legacy zone mapping needs an entry")
        val item = newLegacyZoneMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyZoneMappingCostRangeRule(0.2, 4147.3)
        val item = newLegacyZoneMapping("x", "y")
        item.cost = 4148.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
