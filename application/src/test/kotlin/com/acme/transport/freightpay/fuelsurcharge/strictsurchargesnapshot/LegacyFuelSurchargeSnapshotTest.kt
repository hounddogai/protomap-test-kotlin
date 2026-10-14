package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFuelSurchargeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyFuelSurchargeSnapshot("code-891", "title-271")
        assertEquals(LegacyFuelSurchargeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyFuelSurchargeSnapshot("code-891", "title-271")
        try {
            item.moveTo(LegacyFuelSurchargeSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFuelSurchargeSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyFuelSurchargeSnapshot("code-891", "title-271")
        item.status = LegacyFuelSurchargeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyFuelSurchargeSnapshotStatus.CREATED) }
    }
}
