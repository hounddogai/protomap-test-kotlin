package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledTrailerOverride("code-329", "name-57")
        assertEquals(ScheduledTrailerOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledTrailerOverride("code-329", "name-57")
        try {
            item.moveTo(ScheduledTrailerOverrideStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerOverrideStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledTrailerOverride("code-329", "name-57")
        item.status = ScheduledTrailerOverrideStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledTrailerOverrideStatus.NEW) }
    }
}
