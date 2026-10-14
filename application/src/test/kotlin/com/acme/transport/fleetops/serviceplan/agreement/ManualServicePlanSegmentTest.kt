package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServicePlanSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newManualServicePlanSegment("code-91", "title-318")
        assertEquals(ManualServicePlanSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newManualServicePlanSegment("code-91", "title-318")
        try {
            item.moveTo(ManualServicePlanSegmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServicePlanSegmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualServicePlanSegment("code-91", "title-318")
        item.status = ManualServicePlanSegmentStatus.FAILED
        assertFails("move") { item.moveTo(ManualServicePlanSegmentStatus.NEW) }
    }
}
