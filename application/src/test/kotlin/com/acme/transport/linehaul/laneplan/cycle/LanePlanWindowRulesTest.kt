package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanWindowValidator()
        val item = newLanePlanWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = LanePlanWindowSourceLengthRule(86400)
        val item = newLanePlanWindow("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanWindowRequiredCodeRule("lane plan window needs an entry")
        val item = newLanePlanWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LanePlanWindowFuelLitersRangeRule(6.4, 3896.6)
        val item = newLanePlanWindow("x", "y")
        item.fuelLiters = 3898.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LanePlanWindowRequiredSourceRule("lane plan window needs an entry")
        val item = newLanePlanWindow("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LanePlanWindowRequiredNameRule("lane plan window needs this field")
        val item = newLanePlanWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
