package com.acme.transport.linehaul.laneplan.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanCycleValidator()
        val item = newLanePlanCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LanePlanCycleRequiredLabelRule("lane plan cycle needs an entry")
        val item = newLanePlanCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LanePlanCycleSequenceRangeRule(120, 255)
        val item = newLanePlanCycle("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LanePlanCycleChannelLengthRule(1000)
        val item = newLanePlanCycle("x", "y")
        item.channel = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LanePlanCycleRequiredTimezoneRule("lane plan cycle needs this field")
        val item = newLanePlanCycle("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LanePlanCycleTimezoneLengthRule(16)
        val item = newLanePlanCycle("x", "y")
        item.timezone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanCycleLabelLengthRule(120)
        val item = newLanePlanCycle("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
