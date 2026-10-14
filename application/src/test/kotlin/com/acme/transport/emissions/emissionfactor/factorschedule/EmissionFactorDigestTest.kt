package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newEmissionFactorDigest("code-418", "title-736")
        assertEquals(EmissionFactorDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newEmissionFactorDigest("code-418", "title-736")
        try {
            item.moveTo(EmissionFactorDigestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorDigestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEmissionFactorDigest("code-418", "title-736")
        item.status = EmissionFactorDigestStatus.CLOSED
        assertFails("move") { item.moveTo(EmissionFactorDigestStatus.DRAFT) }
    }
}
