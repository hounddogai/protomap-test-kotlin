package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIdlingSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalIdlingSummary("code-700", "name-754")
        assertEquals(RegionalIdlingSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRegionalIdlingSummary("code-700", "name-754")
        try {
            item.moveTo(RegionalIdlingSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIdlingSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalIdlingSummary("code-700", "name-754")
        item.status = RegionalIdlingSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalIdlingSummaryStatus.PLANNED) }
    }
}
