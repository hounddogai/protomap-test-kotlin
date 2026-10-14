package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newDropTarget("code-209", "title-886")
        assertEquals(DropTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDropTarget("code-209", "title-886")
        try {
            item.moveTo(DropTargetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropTargetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropTarget("code-209", "title-886")
        item.status = DropTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(DropTargetStatus.REQUESTED) }
    }
}
