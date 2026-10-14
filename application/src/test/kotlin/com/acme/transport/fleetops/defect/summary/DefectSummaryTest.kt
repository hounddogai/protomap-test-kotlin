package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectSummary("code-488", "name-181")
        assertEquals(DefectSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefectSummary("code-488", "name-181")
        try {
            item.moveTo(DefectSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectSummary("code-488", "name-181")
        item.status = DefectSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectSummaryStatus.PROPOSED) }
    }
}
