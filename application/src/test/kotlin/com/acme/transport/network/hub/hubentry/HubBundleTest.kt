package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubBundleTest {
    @Test
    fun `starts in created`() {
        val item = newHubBundle("code-195", "label-917")
        assertEquals(HubBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newHubBundle("code-195", "label-917")
        try {
            item.moveTo(HubBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHubBundle("code-195", "label-917")
        item.status = HubBundleStatus.RETIRED
        assertFails("move") { item.moveTo(HubBundleStatus.CREATED) }
    }
}
