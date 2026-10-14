package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeSegment("code-994", "name-385")
        assertEquals(EvChargeSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEvChargeSegment("code-994", "name-385")
        try {
            item.moveTo(EvChargeSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeSegment("code-994", "name-385")
        item.status = EvChargeSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeSegmentStatus.OPEN) }
    }
}
