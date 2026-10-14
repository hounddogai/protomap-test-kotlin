package com.acme.transport.crewing.swap.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapDigest("code-892", "label-437")
        assertEquals(SwapDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newSwapDigest("code-892", "label-437")
        try {
            item.moveTo(SwapDigestStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapDigestStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapDigest("code-892", "label-437")
        item.status = SwapDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapDigestStatus.PLANNED) }
    }
}
