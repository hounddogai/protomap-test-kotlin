package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredParcelBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredParcelBaseline("code-104", "title-154")
        assertEquals(TieredParcelBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newTieredParcelBaseline("code-104", "title-154")
        try {
            item.moveTo(TieredParcelBaselineStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredParcelBaselineStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredParcelBaseline("code-104", "title-154")
        item.status = TieredParcelBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(TieredParcelBaselineStatus.IDLE) }
    }
}
