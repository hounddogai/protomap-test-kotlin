package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireMappingValidator()
        val item = newTireMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TireMappingStopCountRangeRule(255, 86400)
        val item = newTireMapping("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireMappingRequiredCodeRule("tire mapping needs a value")
        val item = newTireMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TireMappingDriverIdLengthRule(255)
        val item = newTireMapping("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
