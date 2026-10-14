package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newHubAdjustment("code-615", "name-217")
        assertEquals(HubAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHubAdjustment("code-615", "name-217")
        try {
            item.moveTo(HubAdjustmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubAdjustmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubAdjustment("code-615", "name-217")
        item.status = HubAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(HubAdjustmentStatus.NEW) }
    }
}
