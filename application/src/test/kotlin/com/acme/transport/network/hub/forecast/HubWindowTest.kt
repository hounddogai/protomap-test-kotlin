package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubWindowTest {
    @Test
    fun `starts in new`() {
        val item = newHubWindow("code-942", "name-706")
        assertEquals(HubWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newHubWindow("code-942", "name-706")
        try {
            item.moveTo(HubWindowStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubWindowStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubWindow("code-942", "name-706")
        item.status = HubWindowStatus.FAILED
        assertFails("move") { item.moveTo(HubWindowStatus.NEW) }
    }
}
