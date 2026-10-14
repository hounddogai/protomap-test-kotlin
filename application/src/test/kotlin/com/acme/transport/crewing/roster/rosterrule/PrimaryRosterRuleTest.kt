package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRosterRuleTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryRosterRule("code-647", "name-387")
        assertEquals(PrimaryRosterRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryRosterRule("code-647", "name-387")
        try {
            item.moveTo(PrimaryRosterRuleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRosterRuleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryRosterRule("code-647", "name-387")
        item.status = PrimaryRosterRuleStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryRosterRuleStatus.NEW) }
    }
}
