package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerTierTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerTier("code-537", "label-806")
        assertEquals(TrailerTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTrailerTier("code-537", "label-806")
        try {
            item.moveTo(TrailerTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerTier("code-537", "label-806")
        item.status = TrailerTierStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerTierStatus.IDLE) }
    }
}
