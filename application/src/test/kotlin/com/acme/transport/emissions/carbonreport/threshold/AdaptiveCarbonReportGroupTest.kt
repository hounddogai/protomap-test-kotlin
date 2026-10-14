package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCarbonReportGroupTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveCarbonReportGroup("code-491", "title-30")
        assertEquals(AdaptiveCarbonReportGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAdaptiveCarbonReportGroup("code-491", "title-30")
        try {
            item.moveTo(AdaptiveCarbonReportGroupStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCarbonReportGroupStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveCarbonReportGroup("code-491", "title-30")
        item.status = AdaptiveCarbonReportGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveCarbonReportGroupStatus.OPEN) }
    }
}
