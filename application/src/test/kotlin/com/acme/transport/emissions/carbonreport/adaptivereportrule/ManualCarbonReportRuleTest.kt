package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCarbonReportRuleTest {
    @Test
    fun `starts in open`() {
        val item = newManualCarbonReportRule("code-389", "label-60")
        assertEquals(ManualCarbonReportRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newManualCarbonReportRule("code-389", "label-60")
        try {
            item.moveTo(ManualCarbonReportRuleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCarbonReportRuleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualCarbonReportRule("code-389", "label-60")
        item.status = ManualCarbonReportRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualCarbonReportRuleStatus.OPEN) }
    }
}
