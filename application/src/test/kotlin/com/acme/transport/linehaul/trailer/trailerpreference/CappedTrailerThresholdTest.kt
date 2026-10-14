package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedTrailerThreshold("code-715", "label-475")
        assertEquals(CappedTrailerThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedTrailerThreshold("code-715", "label-475")
        try {
            item.moveTo(CappedTrailerThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedTrailerThreshold("code-715", "label-475")
        item.status = CappedTrailerThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedTrailerThresholdStatus.PROPOSED) }
    }
}
