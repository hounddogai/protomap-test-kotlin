package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRestBreakMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingRestBreakMapping("code-646", "title-838")
        assertEquals(PendingRestBreakMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPendingRestBreakMapping("code-646", "title-838")
        try {
            item.moveTo(PendingRestBreakMappingStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRestBreakMappingStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingRestBreakMapping("code-646", "title-838")
        item.status = PendingRestBreakMappingStatus.CLOSED
        assertFails("move") { item.moveTo(PendingRestBreakMappingStatus.DRAFT) }
    }
}
