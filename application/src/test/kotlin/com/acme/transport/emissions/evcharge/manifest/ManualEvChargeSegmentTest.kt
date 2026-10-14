package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualEvChargeSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newManualEvChargeSegment("code-189", "name-733")
        assertEquals(ManualEvChargeSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualEvChargeSegment("code-189", "name-733")
        try {
            item.moveTo(ManualEvChargeSegmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualEvChargeSegmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualEvChargeSegment("code-189", "name-733")
        item.status = ManualEvChargeSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualEvChargeSegmentStatus.PLANNED) }
    }
}
