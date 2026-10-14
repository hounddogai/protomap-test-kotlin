package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newTenderSegment("code-545", "label-763")
        assertEquals(TenderSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTenderSegment("code-545", "label-763")
        try {
            item.moveTo(TenderSegmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderSegmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderSegment("code-545", "label-763")
        item.status = TenderSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(TenderSegmentStatus.CREATED) }
    }
}
