package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        assertEquals(LegacyCrossDockSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        try {
            item.moveTo(LegacyCrossDockSummaryStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockSummaryStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        item.status = LegacyCrossDockSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyCrossDockSummaryStatus.PROPOSED) }
    }
}
