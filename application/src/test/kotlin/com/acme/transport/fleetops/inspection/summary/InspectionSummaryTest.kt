package com.acme.transport.fleetops.inspection.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionSummary("code-96", "title-61")
        assertEquals(InspectionSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newInspectionSummary("code-96", "title-61")
        try {
            item.moveTo(InspectionSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionSummary("code-96", "title-61")
        item.status = InspectionSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionSummaryStatus.OPEN) }
    }
}
