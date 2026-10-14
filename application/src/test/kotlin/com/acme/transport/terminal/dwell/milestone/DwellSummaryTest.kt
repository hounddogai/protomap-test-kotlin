package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newDwellSummary("code-968", "title-374")
        assertEquals(DwellSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDwellSummary("code-968", "title-374")
        try {
            item.moveTo(DwellSummaryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellSummaryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDwellSummary("code-968", "title-374")
        item.status = DwellSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(DwellSummaryStatus.CREATED) }
    }
}
