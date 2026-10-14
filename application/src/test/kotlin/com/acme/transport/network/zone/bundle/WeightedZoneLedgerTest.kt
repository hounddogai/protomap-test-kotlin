package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedZoneLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedZoneLedger("code-195", "title-346")
        assertEquals(WeightedZoneLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedZoneLedger("code-195", "title-346")
        try {
            item.moveTo(WeightedZoneLedgerStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedZoneLedgerStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedZoneLedger("code-195", "title-346")
        item.status = WeightedZoneLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedZoneLedgerStatus.OPEN) }
    }
}
