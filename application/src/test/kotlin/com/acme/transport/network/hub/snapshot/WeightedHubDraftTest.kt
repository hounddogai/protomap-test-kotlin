package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedHubDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedHubDraft("code-879", "label-237")
        assertEquals(WeightedHubDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedHubDraft("code-879", "label-237")
        try {
            item.moveTo(WeightedHubDraftStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedHubDraftStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedHubDraft("code-879", "label-237")
        item.status = WeightedHubDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedHubDraftStatus.PLANNED) }
    }
}
