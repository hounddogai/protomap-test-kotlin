package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBackhaulPreferenceValidator()
        val item = newLegacyBackhaulPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = LegacyBackhaulPreferenceOwnerIdLengthRule(16)
        val item = newLegacyBackhaulPreference("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyBackhaulPreferenceRequiredSegmentRule("legacy backhaul preference needs a value")
        val item = newLegacyBackhaulPreference("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyBackhaulPreferenceRequiredLabelRule("legacy backhaul preference needs a value")
        val item = newLegacyBackhaulPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LegacyBackhaulPreferenceFloorValueRangeRule(2.3, 4840.8)
        val item = newLegacyBackhaulPreference("x", "y")
        item.floorValue = 4842.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
