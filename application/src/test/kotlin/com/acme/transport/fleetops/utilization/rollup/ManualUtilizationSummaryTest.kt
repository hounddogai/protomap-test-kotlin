package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newManualUtilizationSummary("code-781", "name-208")
        assertEquals(ManualUtilizationSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualUtilizationSummary("code-781", "name-208")
        try {
            item.moveTo(ManualUtilizationSummaryStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationSummaryStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualUtilizationSummary("code-781", "name-208")
        item.status = ManualUtilizationSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(ManualUtilizationSummaryStatus.DRAFT) }
    }
}
