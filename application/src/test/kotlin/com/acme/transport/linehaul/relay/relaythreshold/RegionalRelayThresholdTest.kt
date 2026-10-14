package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRelayThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalRelayThreshold("code-612", "name-208")
        assertEquals(RegionalRelayThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalRelayThreshold("code-612", "name-208")
        try {
            item.moveTo(RegionalRelayThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRelayThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalRelayThreshold("code-612", "name-208")
        item.status = RegionalRelayThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalRelayThresholdStatus.REQUESTED) }
    }
}
