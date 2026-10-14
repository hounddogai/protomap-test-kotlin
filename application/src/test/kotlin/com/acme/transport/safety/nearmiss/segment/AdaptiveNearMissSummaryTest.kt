package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveNearMissSummary("code-556", "name-568")
        assertEquals(AdaptiveNearMissSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveNearMissSummary("code-556", "name-568")
        try {
            item.moveTo(AdaptiveNearMissSummaryStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissSummaryStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveNearMissSummary("code-556", "name-568")
        item.status = AdaptiveNearMissSummaryStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveNearMissSummaryStatus.NEW) }
    }
}
