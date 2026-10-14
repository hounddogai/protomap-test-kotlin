package com.acme.transport.linehaul.capacity.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newCapacityThreshold("code-586", "name-460")
        assertEquals(CapacityThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCapacityThreshold("code-586", "name-460")
        try {
            item.moveTo(CapacityThresholdStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityThresholdStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCapacityThreshold("code-586", "name-460")
        item.status = CapacityThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(CapacityThresholdStatus.CREATED) }
    }
}
