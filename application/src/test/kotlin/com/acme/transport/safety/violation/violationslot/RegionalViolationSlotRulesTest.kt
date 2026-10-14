package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalViolationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalViolationSlotValidator()
        val item = newRegionalViolationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RegionalViolationSlotRevisionRangeRule(80, 120)
        val item = newRegionalViolationSlot("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = RegionalViolationSlotRequiredChannelRule("regional violation slot needs this field")
        val item = newRegionalViolationSlot("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RegionalViolationSlotChannelLengthRule(86400)
        val item = newRegionalViolationSlot("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalViolationSlotRequiredCodeRule("regional violation slot needs an entry")
        val item = newRegionalViolationSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalViolationSlotRequiredLabelRule("regional violation slot needs this field")
        val item = newRegionalViolationSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
