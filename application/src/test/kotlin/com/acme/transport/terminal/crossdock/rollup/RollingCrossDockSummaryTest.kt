package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCrossDockSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newRollingCrossDockSummary("code-973", "title-938")
        assertEquals(RollingCrossDockSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRollingCrossDockSummary("code-973", "title-938")
        try {
            item.moveTo(RollingCrossDockSummaryStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCrossDockSummaryStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingCrossDockSummary("code-973", "title-938")
        item.status = RollingCrossDockSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingCrossDockSummaryStatus.OPEN) }
    }
}
