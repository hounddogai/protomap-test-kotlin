package com.acme.transport.crewing.roster.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterTargetTest {
    @Test
    fun `starts in created`() {
        val item = newRosterTarget("code-630", "name-407")
        assertEquals(RosterTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRosterTarget("code-630", "name-407")
        try {
            item.moveTo(RosterTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterTarget("code-630", "name-407")
        item.status = RosterTargetStatus.RETIRED
        assertFails("move") { item.moveTo(RosterTargetStatus.CREATED) }
    }
}
