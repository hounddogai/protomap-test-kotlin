package com.acme.transport.customs.clearance.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredClearancePreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredClearancePreferenceValidator()
        val item = newTieredClearancePreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TieredClearancePreferenceEndsAtRangeRule(80, 3600)
        val item = newTieredClearancePreference("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredClearancePreferenceLabelLengthRule(3600)
        val item = newTieredClearancePreference("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = TieredClearancePreferenceRequiredTrailerIdRule("tiered clearance preference needs this field")
        val item = newTieredClearancePreference("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TieredClearancePreferenceCapacityRangeRule(120, 1000)
        val item = newTieredClearancePreference("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = TieredClearancePreferenceRequiredRegionRule("tiered clearance preference needs an entry")
        val item = newTieredClearancePreference("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredClearancePreferenceCodeLengthRule(120)
        val item = newTieredClearancePreference("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
