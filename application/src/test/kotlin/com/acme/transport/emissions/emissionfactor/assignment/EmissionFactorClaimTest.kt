package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorClaim("code-127", "name-475")
        assertEquals(EmissionFactorClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newEmissionFactorClaim("code-127", "name-475")
        try {
            item.moveTo(EmissionFactorClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorClaim("code-127", "name-475")
        item.status = EmissionFactorClaimStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorClaimStatus.IDLE) }
    }
}
