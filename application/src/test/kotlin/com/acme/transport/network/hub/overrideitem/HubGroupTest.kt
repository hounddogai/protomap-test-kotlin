package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubGroupTest {
    @Test
    fun `starts in new`() {
        val item = newHubGroup("code-665", "title-308")
        assertEquals(HubGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHubGroup("code-665", "title-308")
        try {
            item.moveTo(HubGroupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubGroupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubGroup("code-665", "title-308")
        item.status = HubGroupStatus.FAILED
        assertFails("move") { item.moveTo(HubGroupStatus.NEW) }
    }
}
