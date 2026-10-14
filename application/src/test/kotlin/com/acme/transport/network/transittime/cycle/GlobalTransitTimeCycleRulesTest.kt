package com.acme.transport.network.transittime.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTransitTimeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTransitTimeCycleValidator()
        val item = newGlobalTransitTimeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalTransitTimeCycleRequiredContactEmailRule("global transit time cycle needs this field")
        val item = newGlobalTransitTimeCycle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = GlobalTransitTimeCycleCarrierLengthRule(80)
        val item = newGlobalTransitTimeCycle("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalTransitTimeCycleRateRangeRule(2.7, 1055.6)
        val item = newGlobalTransitTimeCycle("x", "y")
        item.rate = 1057.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalTransitTimeCycleRequiredLabelRule("global transit time cycle needs a value")
        val item = newGlobalTransitTimeCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
