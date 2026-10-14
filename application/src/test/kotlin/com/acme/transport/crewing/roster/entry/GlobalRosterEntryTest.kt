package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRosterEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalRosterEntry("code-248", "name-497")
        assertEquals(GlobalRosterEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalRosterEntry("code-248", "name-497")
        try {
            item.moveTo(GlobalRosterEntryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRosterEntryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalRosterEntry("code-248", "name-497")
        item.status = GlobalRosterEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalRosterEntryStatus.PROPOSED) }
    }
}
