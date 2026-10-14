package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEmissionFactorSegmentTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedEmissionFactorSegment("code-265", "title-56")
        assertEquals(BatchedEmissionFactorSegmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedEmissionFactorSegment("code-265", "title-56")
        try {
            item.moveTo(BatchedEmissionFactorSegmentStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEmissionFactorSegmentStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedEmissionFactorSegment("code-265", "title-56")
        item.status = BatchedEmissionFactorSegmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedEmissionFactorSegmentStatus.OPEN) }
    }
}
