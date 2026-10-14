package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCarbonReportPolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        assertEquals(PrimaryCarbonReportPolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        try {
            item.moveTo(PrimaryCarbonReportPolicyStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCarbonReportPolicyStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        item.status = PrimaryCarbonReportPolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryCarbonReportPolicyStatus.PLANNED) }
    }
}
