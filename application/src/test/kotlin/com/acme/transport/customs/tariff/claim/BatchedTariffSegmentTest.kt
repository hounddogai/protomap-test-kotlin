package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTariffSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedTariffSegment("code-544", "name-923")
        assertEquals(BatchedTariffSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedTariffSegment("code-544", "name-923")
        try {
            item.moveTo(BatchedTariffSegmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTariffSegmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedTariffSegment("code-544", "name-923")
        item.status = BatchedTariffSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedTariffSegmentStatus.OPEN) }
    }
}
