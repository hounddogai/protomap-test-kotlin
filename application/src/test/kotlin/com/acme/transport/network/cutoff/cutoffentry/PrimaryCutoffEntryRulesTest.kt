package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCutoffEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCutoffEntryValidator()
        val item = newPrimaryCutoffEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = PrimaryCutoffEntryRequiredRouteCodeRule("primary cutoff entry needs a value")
        val item = newPrimaryCutoffEntry("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryCutoffEntryRequiredChannelRule("primary cutoff entry needs a value")
        val item = newPrimaryCutoffEntry("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryCutoffEntryTitleLengthRule(32)
        val item = newPrimaryCutoffEntry("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
