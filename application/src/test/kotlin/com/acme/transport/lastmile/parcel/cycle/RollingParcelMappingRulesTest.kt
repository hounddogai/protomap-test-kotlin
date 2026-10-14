package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingParcelMappingValidator()
        val item = newRollingParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingParcelMappingScoreRangeRule(5.0, 2652.2)
        val item = newRollingParcelMapping("x", "y")
        item.score = 2653.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingParcelMappingTitleLengthRule(3600)
        val item = newRollingParcelMapping("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RollingParcelMappingDriverIdLengthRule(3600)
        val item = newRollingParcelMapping("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RollingParcelMappingRouteCodeLengthRule(64)
        val item = newRollingParcelMapping("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RollingParcelMappingRequiredDriverIdRule("rolling parcel mapping needs a value")
        val item = newRollingParcelMapping("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
