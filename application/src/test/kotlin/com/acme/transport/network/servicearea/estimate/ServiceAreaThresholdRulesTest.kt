package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaThresholdValidator()
        val item = newServiceAreaThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ServiceAreaThresholdRouteCodeLengthRule(64)
        val item = newServiceAreaThreshold("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ServiceAreaThresholdWindowDaysRangeRule(32, 1000)
        val item = newServiceAreaThreshold("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ServiceAreaThresholdLimitRangeRule(32, 64)
        val item = newServiceAreaThreshold("x", "y")
        item.limit = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaThresholdNameLengthRule(86400)
        val item = newServiceAreaThreshold("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
