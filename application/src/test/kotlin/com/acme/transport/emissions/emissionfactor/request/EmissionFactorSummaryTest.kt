package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newEmissionFactorSummary("code-450", "title-922")
        assertEquals(EmissionFactorSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newEmissionFactorSummary("code-450", "title-922")
        try {
            item.moveTo(EmissionFactorSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEmissionFactorSummary("code-450", "title-922")
        item.status = EmissionFactorSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(EmissionFactorSummaryStatus.PLANNED) }
    }
}
