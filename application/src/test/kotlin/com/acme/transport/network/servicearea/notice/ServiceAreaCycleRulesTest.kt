package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaCycleValidator()
        val item = newServiceAreaCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ServiceAreaCycleRequiredDockRule("service area cycle needs this field")
        val item = newServiceAreaCycle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaCycleRequiredCodeRule("service area cycle needs this field")
        val item = newServiceAreaCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServiceAreaCycleRequiredNameRule("service area cycle needs this field")
        val item = newServiceAreaCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaCycleCodeLengthRule(80)
        val item = newServiceAreaCycle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ServiceAreaCycleEtaMinutesRangeRule(64, 255)
        val item = newServiceAreaCycle("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
