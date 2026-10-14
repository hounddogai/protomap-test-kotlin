package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelSurchargeSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedFuelSurchargeSnapshot("code-699", "label-525")
        assertEquals(WeightedFuelSurchargeSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedFuelSurchargeSnapshot("code-699", "label-525")
        try {
            item.moveTo(WeightedFuelSurchargeSnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelSurchargeSnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedFuelSurchargeSnapshot("code-699", "label-525")
        item.status = WeightedFuelSurchargeSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedFuelSurchargeSnapshotStatus.OPEN) }
    }
}
