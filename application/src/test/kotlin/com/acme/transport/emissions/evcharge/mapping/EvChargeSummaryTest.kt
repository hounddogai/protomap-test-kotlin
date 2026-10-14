package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeSummary("code-288", "title-497")
        assertEquals(EvChargeSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newEvChargeSummary("code-288", "title-497")
        try {
            item.moveTo(EvChargeSummaryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeSummaryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeSummary("code-288", "title-497")
        item.status = EvChargeSummaryStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeSummaryStatus.NEW) }
    }
}
