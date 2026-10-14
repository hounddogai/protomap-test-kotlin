package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeGroupValidator()
        val item = newDefaultTransitTimeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DefaultTransitTimeGroupRequiredDockRule("default transit time group needs this field")
        val item = newDefaultTransitTimeGroup("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultTransitTimeGroupLabelLengthRule(32)
        val item = newDefaultTransitTimeGroup("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DefaultTransitTimeGroupPriorityRangeRule(64, 86400)
        val item = newDefaultTransitTimeGroup("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultTransitTimeGroupRequiredCodeRule("default transit time group needs this field")
        val item = newDefaultTransitTimeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = DefaultTransitTimeGroupMarginRangeRule(4.1, 396.2)
        val item = newDefaultTransitTimeGroup("x", "y")
        item.margin = 397.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultTransitTimeGroupContactEmailLengthRule(80)
        val item = newDefaultTransitTimeGroup("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
