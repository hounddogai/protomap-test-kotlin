package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDropTargetTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalDropTarget("code-825", "title-683")
        assertEquals(RegionalDropTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalDropTarget("code-825", "title-683")
        try {
            item.moveTo(RegionalDropTargetStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDropTargetStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalDropTarget("code-825", "title-683")
        item.status = RegionalDropTargetStatus.FAILED
        assertFails("move") { item.moveTo(RegionalDropTargetStatus.NEW) }
    }
}
