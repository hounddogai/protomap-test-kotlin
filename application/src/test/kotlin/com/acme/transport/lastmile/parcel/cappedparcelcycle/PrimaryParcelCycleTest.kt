package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryParcelCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryParcelCycle("code-382", "name-202")
        assertEquals(PrimaryParcelCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryParcelCycle("code-382", "name-202")
        try {
            item.moveTo(PrimaryParcelCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryParcelCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryParcelCycle("code-382", "name-202")
        item.status = PrimaryParcelCycleStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryParcelCycleStatus.IDLE) }
    }
}
