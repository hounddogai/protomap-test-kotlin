package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newSwapNotice("code-419", "title-511")
        assertEquals(SwapNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSwapNotice("code-419", "title-511")
        try {
            item.moveTo(SwapNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSwapNotice("code-419", "title-511")
        item.status = SwapNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(SwapNoticeStatus.IDLE) }
    }
}
