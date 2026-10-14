package com.acme.transport.crewing.swap.swapnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySwapNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacySwapNotice("code-517", "name-543")
        assertEquals(LegacySwapNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacySwapNotice("code-517", "name-543")
        try {
            item.moveTo(LegacySwapNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySwapNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacySwapNotice("code-517", "name-543")
        item.status = LegacySwapNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(LegacySwapNoticeStatus.IDLE) }
    }
}
