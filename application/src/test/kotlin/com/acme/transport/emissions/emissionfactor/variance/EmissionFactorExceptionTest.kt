package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorException("code-903", "label-785")
        assertEquals(EmissionFactorExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newEmissionFactorException("code-903", "label-785")
        try {
            item.moveTo(EmissionFactorExceptionStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorExceptionStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorException("code-903", "label-785")
        item.status = EmissionFactorExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorExceptionStatus.IDLE) }
    }
}
