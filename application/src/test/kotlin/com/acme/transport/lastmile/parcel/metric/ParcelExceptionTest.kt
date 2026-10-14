package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelException("code-621", "label-842")
        assertEquals(ParcelExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newParcelException("code-621", "label-842")
        try {
            item.moveTo(ParcelExceptionStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelExceptionStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelException("code-621", "label-842")
        item.status = ParcelExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelExceptionStatus.REQUESTED) }
    }
}
