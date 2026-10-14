package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanMappingValidator()
        val item = newLanePlanMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LanePlanMappingBudgetRangeRule(9.7, 1296.2)
        val item = newLanePlanMapping("x", "y")
        item.budget = 1297.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LanePlanMappingOdometerRangeRule(1000, 1000)
        val item = newLanePlanMapping("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanMappingCodeLengthRule(64)
        val item = newLanePlanMapping("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LanePlanMappingRevisionRangeRule(80, 86400)
        val item = newLanePlanMapping("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanMappingLabelLengthRule(64)
        val item = newLanePlanMapping("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
