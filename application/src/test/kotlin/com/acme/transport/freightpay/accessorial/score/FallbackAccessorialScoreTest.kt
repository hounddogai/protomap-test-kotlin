package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAccessorialScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackAccessorialScore("code-18", "label-482")
        assertEquals(FallbackAccessorialScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackAccessorialScore("code-18", "label-482")
        try {
            item.moveTo(FallbackAccessorialScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAccessorialScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackAccessorialScore("code-18", "label-482")
        item.status = FallbackAccessorialScoreStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackAccessorialScoreStatus.IDLE) }
    }
}
