package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeSnapshot("code-226", "title-594")
        assertEquals(FuelSurchargeSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newFuelSurchargeSnapshot("code-226", "title-594")
        try {
            item.moveTo(FuelSurchargeSnapshotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeSnapshotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeSnapshot("code-226", "title-594")
        item.status = FuelSurchargeSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeSnapshotStatus.IDLE) }
    }
}
