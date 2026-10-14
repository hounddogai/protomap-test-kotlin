package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerRecord("code-143", "label-948")
        assertEquals(TrailerRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTrailerRecord("code-143", "label-948")
        try {
            item.moveTo(TrailerRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerRecord("code-143", "label-948")
        item.status = TrailerRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerRecordStatus.PROPOSED) }
    }
}
