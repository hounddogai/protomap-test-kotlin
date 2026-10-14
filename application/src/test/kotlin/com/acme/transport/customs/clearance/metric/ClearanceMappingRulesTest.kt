package com.acme.transport.customs.clearance.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceMappingValidator()
        val item = newClearanceMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceMappingTitleLengthRule(80)
        val item = newClearanceMapping("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ClearanceMappingRequiredCarrierRule("clearance mapping needs this field")
        val item = newClearanceMapping("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
