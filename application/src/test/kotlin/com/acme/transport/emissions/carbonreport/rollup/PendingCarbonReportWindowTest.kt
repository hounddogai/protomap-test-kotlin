package com.acme.transport.emissions.carbonreport.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCarbonReportWindowTest {
    @Test
    fun `starts in new`() {
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        assertEquals(PendingCarbonReportWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        try {
            item.moveTo(PendingCarbonReportWindowStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCarbonReportWindowStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        item.status = PendingCarbonReportWindowStatus.FAILED
        assertFails("move") { item.moveTo(PendingCarbonReportWindowStatus.NEW) }
    }
}
