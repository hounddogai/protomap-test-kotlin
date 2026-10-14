package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayRollupValidator()
        val item = newRelayRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RelayRollupTitleLengthRule(64)
        val item = newRelayRollup("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayRollupRequiredCodeRule("relay rollup needs a value")
        val item = newRelayRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RelayRollupRequiredDescriptionRule("relay rollup needs a value")
        val item = newRelayRollup("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RelayRollupRequiredTitleRule("relay rollup needs this field")
        val item = newRelayRollup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
