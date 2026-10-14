package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerYardSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedTrailerYardSnapshot("code-501", "title-483")
        assertEquals(WeightedTrailerYardSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedTrailerYardSnapshot("code-501", "title-483")
        try {
            item.moveTo(WeightedTrailerYardSnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerYardSnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedTrailerYardSnapshot("code-501", "title-483")
        item.status = WeightedTrailerYardSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedTrailerYardSnapshotStatus.OPEN) }
    }
}
