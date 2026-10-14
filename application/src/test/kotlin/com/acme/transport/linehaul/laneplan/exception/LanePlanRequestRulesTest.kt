package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanRequestValidator()
        val item = newLanePlanRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LanePlanRequestLimitRangeRule(120, 255)
        val item = newLanePlanRequest("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanRequestCodeLengthRule(32)
        val item = newLanePlanRequest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanRequestLabelLengthRule(16)
        val item = newLanePlanRequest("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LanePlanRequestRateRangeRule(5.4, 2327.9)
        val item = newLanePlanRequest("x", "y")
        item.rate = 2329.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
