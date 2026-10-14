package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveCrossDockNotice("code-102", "name-783")
        assertEquals(AdaptiveCrossDockNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveCrossDockNotice("code-102", "name-783")
        try {
            item.moveTo(AdaptiveCrossDockNoticeStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockNoticeStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveCrossDockNotice("code-102", "name-783")
        item.status = AdaptiveCrossDockNoticeStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveCrossDockNoticeStatus.NEW) }
    }
}
