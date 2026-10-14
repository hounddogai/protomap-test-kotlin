package com.acme.transport.network.hub.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveHubScoreTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveHubScore("code-788", "title-379")
        assertEquals(AdaptiveHubScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveHubScore("code-788", "title-379")
        try {
            item.moveTo(AdaptiveHubScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveHubScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveHubScore("code-788", "title-379")
        item.status = AdaptiveHubScoreStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveHubScoreStatus.CREATED) }
    }
}
