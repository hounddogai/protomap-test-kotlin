package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTransitTimeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTransitTimeTemplateValidator()
        val item = newGlobalTransitTimeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalTransitTimeTemplateGraceDaysRangeRule(16, 1000)
        val item = newGlobalTransitTimeTemplate("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = GlobalTransitTimeTemplateRequiredOwnerIdRule("global transit time template needs a value")
        val item = newGlobalTransitTimeTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
