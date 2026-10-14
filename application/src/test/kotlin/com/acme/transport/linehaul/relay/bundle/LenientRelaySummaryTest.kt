package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRelaySummaryTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRelaySummary("code-110", "title-526")
        assertEquals(LenientRelaySummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientRelaySummary("code-110", "title-526")
        try {
            item.moveTo(LenientRelaySummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRelaySummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRelaySummary("code-110", "title-526")
        item.status = LenientRelaySummaryStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRelaySummaryStatus.CREATED) }
    }
}
