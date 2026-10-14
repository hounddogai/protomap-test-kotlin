package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterRule("code-76", "title-251")
        assertEquals(RosterRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRosterRule("code-76", "title-251")
        try {
            item.moveTo(RosterRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterRule("code-76", "title-251")
        item.status = RosterRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterRuleStatus.REQUESTED) }
    }
}
