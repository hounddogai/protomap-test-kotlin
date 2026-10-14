package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredParcelCycleTest {
    @Test
    fun `starts in new`() {
        val item = newTieredParcelCycle("code-510", "title-188")
        assertEquals(TieredParcelCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTieredParcelCycle("code-510", "title-188")
        try {
            item.moveTo(TieredParcelCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredParcelCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredParcelCycle("code-510", "title-188")
        item.status = TieredParcelCycleStatus.FAILED
        assertFails("move") { item.moveTo(TieredParcelCycleStatus.NEW) }
    }
}
