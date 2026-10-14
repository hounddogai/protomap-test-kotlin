package com.acme.transport.emissions.carbonreport.regionalreportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCarbonReportRuleTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyCarbonReportRule("code-97", "title-766")
        assertEquals(LegacyCarbonReportRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newLegacyCarbonReportRule("code-97", "title-766")
        try {
            item.moveTo(LegacyCarbonReportRuleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCarbonReportRuleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyCarbonReportRule("code-97", "title-766")
        item.status = LegacyCarbonReportRuleStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyCarbonReportRuleStatus.CREATED) }
    }
}
