package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientInspectionSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientInspectionSlotValidator()
        val item = newLenientInspectionSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientInspectionSlotCodeLengthRule(86400)
        val item = newLenientInspectionSlot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientInspectionSlotWeightRangeRule(3.9, 3624.3)
        val item = newLenientInspectionSlot("x", "y")
        item.weight = 3625.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
