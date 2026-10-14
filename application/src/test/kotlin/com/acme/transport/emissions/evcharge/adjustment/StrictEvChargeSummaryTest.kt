package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEvChargeSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictEvChargeSummary("code-47", "label-781")
        assertEquals(StrictEvChargeSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newStrictEvChargeSummary("code-47", "label-781")
        try {
            item.moveTo(StrictEvChargeSummaryStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEvChargeSummaryStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictEvChargeSummary("code-47", "label-781")
        item.status = StrictEvChargeSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(StrictEvChargeSummaryStatus.IDLE) }
    }
}
