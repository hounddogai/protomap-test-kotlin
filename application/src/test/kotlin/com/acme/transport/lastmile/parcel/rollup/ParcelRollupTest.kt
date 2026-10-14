package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newParcelRollup("code-856", "label-690")
        assertEquals(ParcelRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newParcelRollup("code-856", "label-690")
        try {
            item.moveTo(ParcelRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newParcelRollup("code-856", "label-690")
        item.status = ParcelRollupStatus.STOPPED
        assertFails("move") { item.moveTo(ParcelRollupStatus.IDLE) }
    }
}
