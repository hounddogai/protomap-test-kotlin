package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerRecordTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalTrailerRecord("code-461", "name-663")
        assertEquals(GlobalTrailerRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newGlobalTrailerRecord("code-461", "name-663")
        try {
            item.moveTo(GlobalTrailerRecordStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerRecordStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalTrailerRecord("code-461", "name-663")
        item.status = GlobalTrailerRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalTrailerRecordStatus.OPEN) }
    }
}
