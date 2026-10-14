package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedInspectionSummary("code-978", "title-696")
        assertEquals(CappedInspectionSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedInspectionSummary("code-978", "title-696")
        try {
            item.moveTo(CappedInspectionSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedInspectionSummary("code-978", "title-696")
        item.status = CappedInspectionSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedInspectionSummaryStatus.PROPOSED) }
    }
}
