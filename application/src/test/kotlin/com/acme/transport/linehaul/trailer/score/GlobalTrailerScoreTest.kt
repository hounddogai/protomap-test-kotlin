package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalTrailerScore("code-951", "name-580")
        assertEquals(GlobalTrailerScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalTrailerScore("code-951", "name-580")
        try {
            item.moveTo(GlobalTrailerScoreStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerScoreStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalTrailerScore("code-951", "name-580")
        item.status = GlobalTrailerScoreStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalTrailerScoreStatus.IDLE) }
    }
}
