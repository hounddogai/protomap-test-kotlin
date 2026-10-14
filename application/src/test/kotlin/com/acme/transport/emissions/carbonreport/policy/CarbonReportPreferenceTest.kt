package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportPreference("code-924", "label-735")
        assertEquals(CarbonReportPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCarbonReportPreference("code-924", "label-735")
        try {
            item.moveTo(CarbonReportPreferenceStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportPreferenceStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportPreference("code-924", "label-735")
        item.status = CarbonReportPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportPreferenceStatus.NEW) }
    }
}
