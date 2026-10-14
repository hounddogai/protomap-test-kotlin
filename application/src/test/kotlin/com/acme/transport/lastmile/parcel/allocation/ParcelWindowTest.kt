package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelWindow("code-421", "name-776")
        assertEquals(ParcelWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newParcelWindow("code-421", "name-776")
        try {
            item.moveTo(ParcelWindowStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelWindowStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelWindow("code-421", "name-776")
        item.status = ParcelWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelWindowStatus.REQUESTED) }
    }
}
