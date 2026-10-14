package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportDraftTest {
    @Test
    fun `starts in open`() {
        val item = newCarbonReportDraft("code-325", "name-702")
        assertEquals(CarbonReportDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCarbonReportDraft("code-325", "name-702")
        try {
            item.moveTo(CarbonReportDraftStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportDraftStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCarbonReportDraft("code-325", "name-702")
        item.status = CarbonReportDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(CarbonReportDraftStatus.OPEN) }
    }
}
