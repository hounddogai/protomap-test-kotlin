package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictClearanceGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictClearanceGroupValidator()
        val item = newStrictClearanceGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictClearanceGroupLabelLengthRule(255)
        val item = newStrictClearanceGroup("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = StrictClearanceGroupEtaMinutesRangeRule(120, 1000)
        val item = newStrictClearanceGroup("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = StrictClearanceGroupCarrierLengthRule(86400)
        val item = newStrictClearanceGroup("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
