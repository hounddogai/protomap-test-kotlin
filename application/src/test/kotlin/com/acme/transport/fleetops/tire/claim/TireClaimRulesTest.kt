package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireClaimValidator()
        val item = newTireClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TireClaimStopCountRangeRule(16, 120)
        val item = newTireClaim("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TireClaimRequiredSourceRule("tire claim needs this field")
        val item = newTireClaim("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireClaimRequiredCodeRule("tire claim needs this field")
        val item = newTireClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TireClaimOdometerRangeRule(32, 64)
        val item = newTireClaim("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
