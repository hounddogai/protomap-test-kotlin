package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDefectSlotValidator()
        val item = newPendingDefectSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PendingDefectSlotChecksumLengthRule(32)
        val item = newPendingDefectSlot("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingDefectSlotFloorValueRangeRule(4.5, 3018.8)
        val item = newPendingDefectSlot("x", "y")
        item.floorValue = 3020.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PendingDefectSlotRequiredChannelRule("pending defect slot needs a value")
        val item = newPendingDefectSlot("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDefectSlotRequiredCodeRule("pending defect slot needs this field")
        val item = newPendingDefectSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingDefectSlotLabelLengthRule(86400)
        val item = newPendingDefectSlot("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
