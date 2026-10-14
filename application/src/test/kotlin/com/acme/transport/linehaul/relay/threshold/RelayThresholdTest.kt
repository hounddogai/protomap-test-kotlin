package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newRelayThreshold("code-824", "title-287")
        assertEquals(RelayThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRelayThreshold("code-824", "title-287")
        try {
            item.moveTo(RelayThresholdStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayThresholdStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRelayThreshold("code-824", "title-287")
        item.status = RelayThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(RelayThresholdStatus.IDLE) }
    }
}
