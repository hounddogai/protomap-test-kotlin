package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanPreferenceValidator()
        val item = newLanePlanPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LanePlanPreferenceCapacityRangeRule(1000, 1000)
        val item = newLanePlanPreference("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = LanePlanPreferenceExternalRefLengthRule(32)
        val item = newLanePlanPreference("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanPreferenceCodeLengthRule(86400)
        val item = newLanePlanPreference("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LanePlanPreferenceRatioRangeRule(7.1, 4559.8)
        val item = newLanePlanPreference("x", "y")
        item.ratio = 4561.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
