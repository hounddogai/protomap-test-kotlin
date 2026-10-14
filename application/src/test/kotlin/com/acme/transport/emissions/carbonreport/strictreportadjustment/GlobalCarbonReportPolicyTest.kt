package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCarbonReportPolicyTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalCarbonReportPolicy("code-660", "name-777")
        assertEquals(GlobalCarbonReportPolicyStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalCarbonReportPolicy("code-660", "name-777")
        try {
            item.moveTo(GlobalCarbonReportPolicyStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCarbonReportPolicyStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalCarbonReportPolicy("code-660", "name-777")
        item.status = GlobalCarbonReportPolicyStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalCarbonReportPolicyStatus.PROPOSED) }
    }
}
