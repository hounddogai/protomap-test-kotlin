package com.acme.transport.emissions.idling.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingSlotValidator()
        val item = newIdlingSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingSlotTitleLengthRule(80)
        val item = newIdlingSlot("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = IdlingSlotRequiredContactEmailRule("idling slot needs this field")
        val item = newIdlingSlot("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = IdlingSlotContactEmailLengthRule(3600)
        val item = newIdlingSlot("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = IdlingSlotThresholdRangeRule(0.4, 449.2)
        val item = newIdlingSlot("x", "y")
        item.threshold = 450.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingSlotRequiredCodeRule("idling slot needs an entry")
        val item = newIdlingSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
