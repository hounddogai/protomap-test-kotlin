package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newTransitTimeVariance("code-753", "title-789")
        assertEquals(TransitTimeVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTransitTimeVariance("code-753", "title-789")
        try {
            item.moveTo(TransitTimeVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTransitTimeVariance("code-753", "title-789")
        item.status = TransitTimeVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(TransitTimeVarianceStatus.IDLE) }
    }
}
