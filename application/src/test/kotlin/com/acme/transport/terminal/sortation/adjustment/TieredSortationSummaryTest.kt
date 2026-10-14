package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredSortationSummary("code-26", "label-887")
        assertEquals(TieredSortationSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTieredSortationSummary("code-26", "label-887")
        try {
            item.moveTo(TieredSortationSummaryStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationSummaryStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredSortationSummary("code-26", "label-887")
        item.status = TieredSortationSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredSortationSummaryStatus.PLANNED) }
    }
}
