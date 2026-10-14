package com.acme.transport.emissions.evcharge.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newEvChargeNotice("code-150", "name-247")
        assertEquals(EvChargeNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEvChargeNotice("code-150", "name-247")
        try {
            item.moveTo(EvChargeNoticeStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeNoticeStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEvChargeNotice("code-150", "name-247")
        item.status = EvChargeNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(EvChargeNoticeStatus.REQUESTED) }
    }
}
