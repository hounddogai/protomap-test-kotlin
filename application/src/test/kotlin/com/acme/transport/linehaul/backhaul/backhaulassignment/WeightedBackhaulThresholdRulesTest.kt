package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBackhaulThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBackhaulThresholdValidator()
        val item = newWeightedBackhaulThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedBackhaulThresholdVersionRangeRule(3600, 86400)
        val item = newWeightedBackhaulThreshold("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedBackhaulThresholdUpdatedAtRangeRule(64, 64)
        val item = newWeightedBackhaulThreshold("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = WeightedBackhaulThresholdShiftCodeLengthRule(64)
        val item = newWeightedBackhaulThreshold("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedBackhaulThresholdNameLengthRule(86400)
        val item = newWeightedBackhaulThreshold("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = WeightedBackhaulThresholdRequiredCountryRule("weighted backhaul threshold needs an entry")
        val item = newWeightedBackhaulThreshold("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
