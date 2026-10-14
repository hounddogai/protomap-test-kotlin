package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingNodeMapping("code-872", "title-846")
        assertEquals(PendingNodeMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingNodeMapping("code-872", "title-846")
        try {
            item.moveTo(PendingNodeMappingStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeMappingStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingNodeMapping("code-872", "title-846")
        item.status = PendingNodeMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingNodeMappingStatus.PLANNED) }
    }
}
