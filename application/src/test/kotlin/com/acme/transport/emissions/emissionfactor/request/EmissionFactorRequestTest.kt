package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newEmissionFactorRequest("code-897", "label-664")
        assertEquals(EmissionFactorRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newEmissionFactorRequest("code-897", "label-664")
        try {
            item.moveTo(EmissionFactorRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEmissionFactorRequest("code-897", "label-664")
        item.status = EmissionFactorRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(EmissionFactorRequestStatus.PLANNED) }
    }
}
