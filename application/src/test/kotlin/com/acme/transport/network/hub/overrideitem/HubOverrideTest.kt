package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newHubOverride("code-972", "title-921")
        assertEquals(HubOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newHubOverride("code-972", "title-921")
        try {
            item.moveTo(HubOverrideStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubOverrideStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubOverride("code-972", "title-921")
        item.status = HubOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(HubOverrideStatus.DRAFT) }
    }
}
