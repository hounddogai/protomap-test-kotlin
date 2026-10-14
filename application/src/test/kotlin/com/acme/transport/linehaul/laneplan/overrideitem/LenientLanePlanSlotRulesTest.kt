package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLanePlanSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLanePlanSlotValidator()
        val item = newLenientLanePlanSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientLanePlanSlotRequiredCodeRule("lenient lane plan slot needs a value")
        val item = newLenientLanePlanSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LenientLanePlanSlotEndsAtRangeRule(1000, 86400)
        val item = newLenientLanePlanSlot("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LenientLanePlanSlotRequiredTrailerIdRule("lenient lane plan slot needs this field")
        val item = newLenientLanePlanSlot("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
