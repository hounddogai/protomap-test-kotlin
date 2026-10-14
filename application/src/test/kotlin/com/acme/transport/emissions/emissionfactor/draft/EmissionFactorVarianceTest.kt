package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorVariance("code-18", "name-281")
        assertEquals(EmissionFactorVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEmissionFactorVariance("code-18", "name-281")
        try {
            item.moveTo(EmissionFactorVarianceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorVarianceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorVariance("code-18", "name-281")
        item.status = EmissionFactorVarianceStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorVarianceStatus.NEW) }
    }
}
