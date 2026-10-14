package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newBackhaulRollup("code-155", "name-676")
        assertEquals(BackhaulRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newBackhaulRollup("code-155", "name-676")
        try {
            item.moveTo(BackhaulRollupStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulRollupStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBackhaulRollup("code-155", "name-676")
        item.status = BackhaulRollupStatus.CLOSED
        assertFails("move") { item.moveTo(BackhaulRollupStatus.DRAFT) }
    }
}
