package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCrossDockSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newManualCrossDockSummary("code-244", "name-364")
        assertEquals(ManualCrossDockSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newManualCrossDockSummary("code-244", "name-364")
        try {
            item.moveTo(ManualCrossDockSummaryStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCrossDockSummaryStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualCrossDockSummary("code-244", "name-364")
        item.status = ManualCrossDockSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(ManualCrossDockSummaryStatus.IDLE) }
    }
}
