package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCutoffRecordTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveCutoffRecord("code-223", "title-530")
        assertEquals(AdaptiveCutoffRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveCutoffRecord("code-223", "title-530")
        try {
            item.moveTo(AdaptiveCutoffRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCutoffRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveCutoffRecord("code-223", "title-530")
        item.status = AdaptiveCutoffRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveCutoffRecordStatus.OPEN) }
    }
}
