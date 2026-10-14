package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientParcelRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientParcelRollup("code-358", "name-596")
        assertEquals(LenientParcelRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLenientParcelRollup("code-358", "name-596")
        try {
            item.moveTo(LenientParcelRollupStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientParcelRollupStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientParcelRollup("code-358", "name-596")
        item.status = LenientParcelRollupStatus.CLOSED
        assertFails("move") { item.moveTo(LenientParcelRollupStatus.DRAFT) }
    }
}
