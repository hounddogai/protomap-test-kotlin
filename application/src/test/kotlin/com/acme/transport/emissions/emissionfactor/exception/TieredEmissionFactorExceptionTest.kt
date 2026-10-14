package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEmissionFactorExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredEmissionFactorException("code-997", "title-843")
        assertEquals(TieredEmissionFactorExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredEmissionFactorException("code-997", "title-843")
        try {
            item.moveTo(TieredEmissionFactorExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEmissionFactorExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredEmissionFactorException("code-997", "title-843")
        item.status = TieredEmissionFactorExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredEmissionFactorExceptionStatus.REQUESTED) }
    }
}
