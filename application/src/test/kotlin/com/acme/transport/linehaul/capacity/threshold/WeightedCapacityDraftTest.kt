package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCapacityDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedCapacityDraft("code-448", "title-769")
        assertEquals(WeightedCapacityDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newWeightedCapacityDraft("code-448", "title-769")
        try {
            item.moveTo(WeightedCapacityDraftStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCapacityDraftStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedCapacityDraft("code-448", "title-769")
        item.status = WeightedCapacityDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedCapacityDraftStatus.REQUESTED) }
    }
}
