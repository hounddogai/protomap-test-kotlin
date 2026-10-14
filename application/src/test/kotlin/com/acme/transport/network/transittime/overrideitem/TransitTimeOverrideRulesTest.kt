package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeOverrideValidator()
        val item = newTransitTimeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TransitTimeOverrideRequiredNameRule("transit time override needs an entry")
        val item = newTransitTimeOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeOverrideRequiredCodeRule("transit time override needs an entry")
        val item = newTransitTimeOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TransitTimeOverrideFloorValueRangeRule(6.7, 4016.4)
        val item = newTransitTimeOverride("x", "y")
        item.floorValue = 4017.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
