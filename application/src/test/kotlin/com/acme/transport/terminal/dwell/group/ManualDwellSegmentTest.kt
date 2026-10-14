package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newManualDwellSegment("code-207", "name-920")
        assertEquals(ManualDwellSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newManualDwellSegment("code-207", "name-920")
        try {
            item.moveTo(ManualDwellSegmentStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellSegmentStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualDwellSegment("code-207", "name-920")
        item.status = ManualDwellSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualDwellSegmentStatus.OPEN) }
    }
}
