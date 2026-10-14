package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRosterGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRosterGroup("code-285", "title-577")
        assertEquals(GlobalRosterGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalRosterGroup("code-285", "title-577")
        try {
            item.moveTo(GlobalRosterGroupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRosterGroupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRosterGroup("code-285", "title-577")
        item.status = GlobalRosterGroupStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRosterGroupStatus.DRAFT) }
    }
}
