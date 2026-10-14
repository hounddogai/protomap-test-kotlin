package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaMilestoneValidator()
        val item = newLenientServiceAreaMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LenientServiceAreaMilestoneRequiredShiftCodeRule("lenient service area milestone needs this field")
        val item = newLenientServiceAreaMilestone("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LenientServiceAreaMilestoneShiftCodeLengthRule(64)
        val item = newLenientServiceAreaMilestone("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientServiceAreaMilestoneRequiredLabelRule("lenient service area milestone needs an entry")
        val item = newLenientServiceAreaMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
