package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelSurchargeSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalFuelSurchargeSnapshot("code-853", "name-125")
        assertEquals(RegionalFuelSurchargeSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalFuelSurchargeSnapshot("code-853", "name-125")
        try {
            item.moveTo(RegionalFuelSurchargeSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelSurchargeSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalFuelSurchargeSnapshot("code-853", "name-125")
        item.status = RegionalFuelSurchargeSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalFuelSurchargeSnapshotStatus.NEW) }
    }
}
