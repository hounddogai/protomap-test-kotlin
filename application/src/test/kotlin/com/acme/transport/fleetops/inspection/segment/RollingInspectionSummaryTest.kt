package com.acme.transport.fleetops.inspection.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingInspectionSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newRollingInspectionSummary("code-227", "title-948")
        assertEquals(RollingInspectionSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingInspectionSummary("code-227", "title-948")
        try {
            item.moveTo(RollingInspectionSummaryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingInspectionSummaryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingInspectionSummary("code-227", "title-948")
        item.status = RollingInspectionSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(RollingInspectionSummaryStatus.CREATED) }
    }
}
