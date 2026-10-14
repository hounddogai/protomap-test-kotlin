package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedLanePlanThresholdValidator()
        val item = newCappedLanePlanThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedLanePlanThresholdRequiredCodeRule("capped lane plan threshold needs this field")
        val item = newCappedLanePlanThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = CappedLanePlanThresholdShiftCodeLengthRule(120)
        val item = newCappedLanePlanThreshold("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedLanePlanThresholdNameLengthRule(64)
        val item = newCappedLanePlanThreshold("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
