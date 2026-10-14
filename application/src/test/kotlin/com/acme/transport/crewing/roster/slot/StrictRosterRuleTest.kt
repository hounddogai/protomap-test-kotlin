package com.acme.transport.crewing.roster.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictRosterRule("code-805", "title-445")
        assertEquals(StrictRosterRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newStrictRosterRule("code-805", "title-445")
        try {
            item.moveTo(StrictRosterRuleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterRuleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictRosterRule("code-805", "title-445")
        item.status = StrictRosterRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictRosterRuleStatus.PROPOSED) }
    }
}
