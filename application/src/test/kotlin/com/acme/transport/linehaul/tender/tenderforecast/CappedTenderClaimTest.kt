package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTenderClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedTenderClaim("code-308", "name-936")
        assertEquals(CappedTenderClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedTenderClaim("code-308", "name-936")
        try {
            item.moveTo(CappedTenderClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTenderClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedTenderClaim("code-308", "name-936")
        item.status = CappedTenderClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedTenderClaimStatus.REQUESTED) }
    }
}
