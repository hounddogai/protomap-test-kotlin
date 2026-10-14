package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newBackhaulScore("code-531", "label-890")
        assertEquals(BackhaulScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newBackhaulScore("code-531", "label-890")
        try {
            item.moveTo(BackhaulScoreStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulScoreStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBackhaulScore("code-531", "label-890")
        item.status = BackhaulScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(BackhaulScoreStatus.PLANNED) }
    }
}
