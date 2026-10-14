package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newParcelQuota("code-423", "title-287")
        assertEquals(ParcelQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newParcelQuota("code-423", "title-287")
        try {
            item.moveTo(ParcelQuotaStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelQuotaStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newParcelQuota("code-423", "title-287")
        item.status = ParcelQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(ParcelQuotaStatus.DRAFT) }
    }
}
