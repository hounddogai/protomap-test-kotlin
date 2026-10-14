package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedQualificationSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedQualificationSegment("code-775", "title-654")
        assertEquals(CappedQualificationSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newCappedQualificationSegment("code-775", "title-654")
        try {
            item.moveTo(CappedQualificationSegmentStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedQualificationSegmentStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedQualificationSegment("code-775", "title-654")
        item.status = CappedQualificationSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(CappedQualificationSegmentStatus.DRAFT) }
    }
}
