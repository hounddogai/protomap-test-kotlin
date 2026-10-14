package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newTireThreshold("code-491", "title-627")
        assertEquals(TireThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTireThreshold("code-491", "title-627")
        try {
            item.moveTo(TireThresholdStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireThresholdStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireThreshold("code-491", "title-627")
        item.status = TireThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(TireThresholdStatus.PLANNED) }
    }
}
