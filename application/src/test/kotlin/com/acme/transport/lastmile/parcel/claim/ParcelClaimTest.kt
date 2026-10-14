package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newParcelClaim("code-502", "name-428")
        assertEquals(ParcelClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newParcelClaim("code-502", "name-428")
        try {
            item.moveTo(ParcelClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newParcelClaim("code-502", "name-428")
        item.status = ParcelClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(ParcelClaimStatus.PLANNED) }
    }
}
