package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelSlotTest {
    @Test
    fun `starts in created`() {
        val item = newLenientParcelSlot("code-633", "name-861")
        assertEquals(LenientParcelSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientParcelSlot("code-633", "name-861")
        try {
            item.moveTo(LenientParcelSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientParcelSlot("code-633", "name-861")
        item.status = LenientParcelSlotStatus.RETIRED
        assertFails("move") { item.moveTo(LenientParcelSlotStatus.CREATED) }
    }
}
