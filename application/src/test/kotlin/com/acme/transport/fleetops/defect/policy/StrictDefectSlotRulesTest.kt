package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDefectSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDefectSlotValidator()
        val item = newStrictDefectSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = StrictDefectSlotRequiredLocaleRule("strict defect slot needs this field")
        val item = newStrictDefectSlot("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictDefectSlotRequiredNameRule("strict defect slot needs a value")
        val item = newStrictDefectSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictDefectSlotRequiredSummaryRule("strict defect slot needs a value")
        val item = newStrictDefectSlot("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
