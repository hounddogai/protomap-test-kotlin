package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportRuleTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportRule("code-260", "name-933")
        assertEquals(CarbonReportRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCarbonReportRule("code-260", "name-933")
        try {
            item.moveTo(CarbonReportRuleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportRuleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportRule("code-260", "name-933")
        item.status = CarbonReportRuleStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportRuleStatus.NEW) }
    }
}
