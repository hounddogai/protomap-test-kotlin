package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTrailerProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingTrailerProfile("code-216", "name-917")
        assertEquals(PendingTrailerProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPendingTrailerProfile("code-216", "name-917")
        try {
            item.moveTo(PendingTrailerProfileStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTrailerProfileStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingTrailerProfile("code-216", "name-917")
        item.status = PendingTrailerProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingTrailerProfileStatus.PROPOSED) }
    }
}
