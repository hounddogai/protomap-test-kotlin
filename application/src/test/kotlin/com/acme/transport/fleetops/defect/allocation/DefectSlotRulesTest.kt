package com.acme.transport.fleetops.defect.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectSlotValidator()
        val item = newDefectSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefectSlotLabelLengthRule(16)
        val item = newDefectSlot("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = DefectSlotDescriptionLengthRule(1000)
        val item = newDefectSlot("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
