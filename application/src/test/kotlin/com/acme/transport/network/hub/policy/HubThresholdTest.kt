package com.acme.transport.network.hub.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newHubThreshold("code-295", "name-52")
        assertEquals(HubThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHubThreshold("code-295", "name-52")
        try {
            item.moveTo(HubThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHubThreshold("code-295", "name-52")
        item.status = HubThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(HubThresholdStatus.REQUESTED) }
    }
}
