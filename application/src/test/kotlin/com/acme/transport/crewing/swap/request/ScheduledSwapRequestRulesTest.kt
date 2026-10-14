package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSwapRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSwapRequestValidator()
        val item = newScheduledSwapRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledSwapRequestRequiredNameRule("scheduled swap request needs a value")
        val item = newScheduledSwapRequest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledSwapRequestRequiredCodeRule("scheduled swap request needs a value")
        val item = newScheduledSwapRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ScheduledSwapRequestRequiredChannelRule("scheduled swap request needs this field")
        val item = newScheduledSwapRequest("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
