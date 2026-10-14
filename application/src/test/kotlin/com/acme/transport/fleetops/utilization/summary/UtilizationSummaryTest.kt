package com.acme.transport.fleetops.utilization.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newUtilizationSummary("code-601", "label-287")
        assertEquals(UtilizationSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newUtilizationSummary("code-601", "label-287")
        try {
            item.moveTo(UtilizationSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newUtilizationSummary("code-601", "label-287")
        item.status = UtilizationSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(UtilizationSummaryStatus.PROPOSED) }
    }
}
