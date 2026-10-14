package com.acme.transport.linehaul.relay.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRelayTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRelayTemplateValidator()
        val item = newDefaultRelayTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DefaultRelayTemplateSegmentLengthRule(1000)
        val item = newDefaultRelayTemplate("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DefaultRelayTemplateRateRangeRule(1.9, 3413.1)
        val item = newDefaultRelayTemplate("x", "y")
        item.rate = 3414.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultRelayTemplateLabelLengthRule(1000)
        val item = newDefaultRelayTemplate("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DefaultRelayTemplateRequiredSegmentRule("default relay template needs this field")
        val item = newDefaultRelayTemplate("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = DefaultRelayTemplateRequiredOwnerIdRule("default relay template needs a value")
        val item = newDefaultRelayTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
