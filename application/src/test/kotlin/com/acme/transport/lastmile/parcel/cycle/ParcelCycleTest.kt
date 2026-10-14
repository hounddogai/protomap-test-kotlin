package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelCycleTest {
    @Test
    fun `starts in created`() {
        val item = newParcelCycle("code-63", "title-782")
        assertEquals(ParcelCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newParcelCycle("code-63", "title-782")
        try {
            item.moveTo(ParcelCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelCycle("code-63", "title-782")
        item.status = ParcelCycleStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelCycleStatus.CREATED) }
    }
}
