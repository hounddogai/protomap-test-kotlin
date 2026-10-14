package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeThreshold("code-603", "title-470")
        assertEquals(TransitTimeThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTransitTimeThreshold("code-603", "title-470")
        try {
            item.moveTo(TransitTimeThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeThreshold("code-603", "title-470")
        item.status = TransitTimeThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeThresholdStatus.REQUESTED) }
    }
}
