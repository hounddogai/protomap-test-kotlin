package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelRequest("code-260", "label-253")
        assertEquals(ParcelRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newParcelRequest("code-260", "label-253")
        try {
            item.moveTo(ParcelRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelRequest("code-260", "label-253")
        item.status = ParcelRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelRequestStatus.REQUESTED) }
    }
}
