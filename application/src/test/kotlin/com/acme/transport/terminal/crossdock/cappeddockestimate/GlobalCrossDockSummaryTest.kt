package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCrossDockSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalCrossDockSummary("code-476", "title-622")
        assertEquals(GlobalCrossDockSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalCrossDockSummary("code-476", "title-622")
        try {
            item.moveTo(GlobalCrossDockSummaryStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCrossDockSummaryStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalCrossDockSummary("code-476", "title-622")
        item.status = GlobalCrossDockSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalCrossDockSummaryStatus.IDLE) }
    }
}
