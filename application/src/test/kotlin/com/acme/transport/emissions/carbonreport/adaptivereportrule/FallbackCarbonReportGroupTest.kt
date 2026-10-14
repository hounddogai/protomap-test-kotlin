package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCarbonReportGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        assertEquals(FallbackCarbonReportGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        try {
            item.moveTo(FallbackCarbonReportGroupStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCarbonReportGroupStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        item.status = FallbackCarbonReportGroupStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackCarbonReportGroupStatus.DRAFT) }
    }
}
