package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRelayNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalRelayNotice("code-364", "name-937")
        assertEquals(RegionalRelayNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRegionalRelayNotice("code-364", "name-937")
        try {
            item.moveTo(RegionalRelayNoticeStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRelayNoticeStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalRelayNotice("code-364", "name-937")
        item.status = RegionalRelayNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalRelayNoticeStatus.IDLE) }
    }
}
