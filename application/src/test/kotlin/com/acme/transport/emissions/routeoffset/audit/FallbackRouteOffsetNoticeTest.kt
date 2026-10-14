package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackRouteOffsetNotice("code-885", "name-69")
        assertEquals(FallbackRouteOffsetNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackRouteOffsetNotice("code-885", "name-69")
        try {
            item.moveTo(FallbackRouteOffsetNoticeStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetNoticeStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackRouteOffsetNotice("code-885", "name-69")
        item.status = FallbackRouteOffsetNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackRouteOffsetNoticeStatus.IDLE) }
    }
}
