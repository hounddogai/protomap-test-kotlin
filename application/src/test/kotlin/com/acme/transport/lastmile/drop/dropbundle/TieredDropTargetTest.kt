package com.acme.transport.lastmile.drop.dropbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDropTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredDropTarget("code-281", "title-98")
        assertEquals(TieredDropTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTieredDropTarget("code-281", "title-98")
        try {
            item.moveTo(TieredDropTargetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDropTargetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredDropTarget("code-281", "title-98")
        item.status = TieredDropTargetStatus.CLOSED
        assertFails("move") { item.moveTo(TieredDropTargetStatus.DRAFT) }
    }
}
