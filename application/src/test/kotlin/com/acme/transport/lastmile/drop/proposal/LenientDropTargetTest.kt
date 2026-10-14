package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDropTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientDropTarget("code-923", "title-244")
        assertEquals(LenientDropTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientDropTarget("code-923", "title-244")
        try {
            item.moveTo(LenientDropTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDropTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientDropTarget("code-923", "title-244")
        item.status = LenientDropTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientDropTargetStatus.PROPOSED) }
    }
}
