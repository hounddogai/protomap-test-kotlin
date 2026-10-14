package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerAdjustment("code-451", "title-968")
        assertEquals(TrailerAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTrailerAdjustment("code-451", "title-968")
        try {
            item.moveTo(TrailerAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerAdjustment("code-451", "title-968")
        item.status = TrailerAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(TrailerAdjustmentStatus.NEW) }
    }
}
