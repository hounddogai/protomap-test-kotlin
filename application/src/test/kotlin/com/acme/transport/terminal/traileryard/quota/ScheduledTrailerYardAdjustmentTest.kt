package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerYardAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledTrailerYardAdjustment("code-530", "name-221")
        assertEquals(ScheduledTrailerYardAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledTrailerYardAdjustment("code-530", "name-221")
        try {
            item.moveTo(ScheduledTrailerYardAdjustmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerYardAdjustmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledTrailerYardAdjustment("code-530", "name-221")
        item.status = ScheduledTrailerYardAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledTrailerYardAdjustmentStatus.CREATED) }
    }
}
