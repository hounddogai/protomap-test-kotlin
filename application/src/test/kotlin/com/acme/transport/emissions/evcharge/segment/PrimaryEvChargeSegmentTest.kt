package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryEvChargeSegment("code-646", "label-24")
        assertEquals(PrimaryEvChargeSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryEvChargeSegment("code-646", "label-24")
        try {
            item.moveTo(PrimaryEvChargeSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryEvChargeSegment("code-646", "label-24")
        item.status = PrimaryEvChargeSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryEvChargeSegmentStatus.IDLE) }
    }
}
