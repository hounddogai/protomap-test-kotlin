package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropRollupTest {
    @Test
    fun `starts in open`() {
        val item = newDropRollup("code-514", "name-870")
        assertEquals(DropRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDropRollup("code-514", "name-870")
        try {
            item.moveTo(DropRollupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropRollupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDropRollup("code-514", "name-870")
        item.status = DropRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(DropRollupStatus.OPEN) }
    }
}
