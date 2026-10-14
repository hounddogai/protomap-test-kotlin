package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCarbonReportPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackCarbonReportPreference("code-683", "name-275")
        assertEquals(FallbackCarbonReportPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackCarbonReportPreference("code-683", "name-275")
        try {
            item.moveTo(FallbackCarbonReportPreferenceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCarbonReportPreferenceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackCarbonReportPreference("code-683", "name-275")
        item.status = FallbackCarbonReportPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackCarbonReportPreferenceStatus.OPEN) }
    }
}
