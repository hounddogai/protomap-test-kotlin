package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubTier("code-53", "title-773")
        assertEquals(HubTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHubTier("code-53", "title-773")
        try {
            item.moveTo(HubTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubTier("code-53", "title-773")
        item.status = HubTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubTierStatus.PROPOSED) }
    }
}
