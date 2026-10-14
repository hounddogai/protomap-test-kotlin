package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        assertEquals(SeasonalEmissionFactorExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        try {
            item.moveTo(SeasonalEmissionFactorExceptionStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorExceptionStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        item.status = SeasonalEmissionFactorExceptionStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalEmissionFactorExceptionStatus.NEW) }
    }
}
