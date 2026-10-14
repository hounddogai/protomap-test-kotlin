package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelAdjustment("code-577", "name-495")
        assertEquals(ParcelAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newParcelAdjustment("code-577", "name-495")
        try {
            item.moveTo(ParcelAdjustmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelAdjustmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelAdjustment("code-577", "name-495")
        item.status = ParcelAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelAdjustmentStatus.REQUESTED) }
    }
}
