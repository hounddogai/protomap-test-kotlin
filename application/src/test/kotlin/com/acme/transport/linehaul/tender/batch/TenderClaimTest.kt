package com.acme.transport.linehaul.tender.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderClaim("code-48", "name-63")
        assertEquals(TenderClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTenderClaim("code-48", "name-63")
        try {
            item.moveTo(TenderClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderClaim("code-48", "name-63")
        item.status = TenderClaimStatus.STOPPED
        assertFails("move") { item.moveTo(TenderClaimStatus.IDLE) }
    }
}
