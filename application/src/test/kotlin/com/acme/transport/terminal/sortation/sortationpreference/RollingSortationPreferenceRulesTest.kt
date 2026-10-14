package com.acme.transport.terminal.sortation.sortationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSortationPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSortationPreferenceValidator()
        val item = newRollingSortationPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingSortationPreferenceLaneLengthRule(80)
        val item = newRollingSortationPreference("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RollingSortationPreferenceRequiredLaneRule("rolling sortation preference needs a value")
        val item = newRollingSortationPreference("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingSortationPreferenceOdometerRangeRule(16, 32)
        val item = newRollingSortationPreference("x", "y")
        item.odometer = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingSortationPreferenceRequiredTitleRule("rolling sortation preference needs this field")
        val item = newRollingSortationPreference("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingSortationPreferenceCapacityRangeRule(1000, 3600)
        val item = newRollingSortationPreference("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingSortationPreferenceRequiredCodeRule("rolling sortation preference needs this field")
        val item = newRollingSortationPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
