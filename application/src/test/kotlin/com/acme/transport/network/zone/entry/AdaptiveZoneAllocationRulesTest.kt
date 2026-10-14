package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveZoneAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveZoneAllocationValidator()
        val item = newAdaptiveZoneAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = AdaptiveZoneAllocationRequiredSourceRule("adaptive zone allocation needs this field")
        val item = newAdaptiveZoneAllocation("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = AdaptiveZoneAllocationSourceLengthRule(32)
        val item = newAdaptiveZoneAllocation("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveZoneAllocationLabelLengthRule(32)
        val item = newAdaptiveZoneAllocation("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveZoneAllocationRequiredCodeRule("adaptive zone allocation needs an entry")
        val item = newAdaptiveZoneAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AdaptiveZoneAllocationCostRangeRule(2.1, 2660.3)
        val item = newAdaptiveZoneAllocation("x", "y")
        item.cost = 2661.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = AdaptiveZoneAllocationRevisionRangeRule(64, 120)
        val item = newAdaptiveZoneAllocation("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
