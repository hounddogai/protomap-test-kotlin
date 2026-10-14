package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubManifestTest {
    @Test
    fun `starts in created`() {
        val item = newHubManifest("code-270", "label-333")
        assertEquals(HubManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newHubManifest("code-270", "label-333")
        try {
            item.moveTo(HubManifestStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubManifestStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHubManifest("code-270", "label-333")
        item.status = HubManifestStatus.RETIRED
        assertFails("move") { item.moveTo(HubManifestStatus.CREATED) }
    }
}
