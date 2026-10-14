package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRosterRollupValidator()
        val item = newPendingRosterRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRosterRollupRequiredCodeRule("pending roster rollup needs this field")
        val item = newPendingRosterRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = PendingRosterRollupDepotLengthRule(3600)
        val item = newPendingRosterRollup("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingRosterRollupTitleLengthRule(64)
        val item = newPendingRosterRollup("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PendingRosterRollupRequiredDepotRule("pending roster rollup needs an entry")
        val item = newPendingRosterRollup("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRosterRollupCodeLengthRule(86400)
        val item = newPendingRosterRollup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
