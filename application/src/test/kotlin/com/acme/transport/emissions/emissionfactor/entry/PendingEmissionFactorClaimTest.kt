package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEmissionFactorClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingEmissionFactorClaim("code-957", "label-663")
        assertEquals(PendingEmissionFactorClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingEmissionFactorClaim("code-957", "label-663")
        try {
            item.moveTo(PendingEmissionFactorClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEmissionFactorClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingEmissionFactorClaim("code-957", "label-663")
        item.status = PendingEmissionFactorClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingEmissionFactorClaimStatus.REQUESTED) }
    }
}
