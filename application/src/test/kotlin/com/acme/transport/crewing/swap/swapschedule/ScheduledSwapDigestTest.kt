package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledSwapDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledSwapDigest("code-70", "title-683")
        assertEquals(ScheduledSwapDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newScheduledSwapDigest("code-70", "title-683")
        try {
            item.moveTo(ScheduledSwapDigestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledSwapDigestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledSwapDigest("code-70", "title-683")
        item.status = ScheduledSwapDigestStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledSwapDigestStatus.DRAFT) }
    }
}
