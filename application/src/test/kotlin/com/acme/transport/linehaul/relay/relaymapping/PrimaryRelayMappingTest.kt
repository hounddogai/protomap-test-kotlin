package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRelayMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryRelayMapping("code-926", "title-287")
        assertEquals(PrimaryRelayMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPrimaryRelayMapping("code-926", "title-287")
        try {
            item.moveTo(PrimaryRelayMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRelayMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryRelayMapping("code-926", "title-287")
        item.status = PrimaryRelayMappingStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryRelayMappingStatus.IDLE) }
    }
}
