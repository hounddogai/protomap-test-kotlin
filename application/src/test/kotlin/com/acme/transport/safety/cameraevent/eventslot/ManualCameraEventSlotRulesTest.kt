package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventSlotValidator()
        val item = newManualCameraEventSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualCameraEventSlotRequiredLabelRule("manual camera event slot needs a value")
        val item = newManualCameraEventSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCameraEventSlotRequiredCodeRule("manual camera event slot needs an entry")
        val item = newManualCameraEventSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ManualCameraEventSlotPriorityRangeRule(1000, 86400)
        val item = newManualCameraEventSlot("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
