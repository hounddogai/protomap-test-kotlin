package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapTarget("code-650", "name-971")
        assertEquals(SwapTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSwapTarget("code-650", "name-971")
        try {
            item.moveTo(SwapTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapTarget("code-650", "name-971")
        item.status = SwapTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapTargetStatus.PLANNED) }
    }
}
