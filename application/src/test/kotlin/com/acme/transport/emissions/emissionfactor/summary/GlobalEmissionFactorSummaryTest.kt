package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalEmissionFactorSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalEmissionFactorSummary("code-493", "label-962")
        assertEquals(GlobalEmissionFactorSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalEmissionFactorSummary("code-493", "label-962")
        try {
            item.moveTo(GlobalEmissionFactorSummaryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalEmissionFactorSummaryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalEmissionFactorSummary("code-493", "label-962")
        item.status = GlobalEmissionFactorSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalEmissionFactorSummaryStatus.REQUESTED) }
    }
}
