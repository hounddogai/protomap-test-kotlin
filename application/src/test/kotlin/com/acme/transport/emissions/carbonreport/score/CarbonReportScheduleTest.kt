package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportSchedule("code-438", "name-393")
        assertEquals(CarbonReportScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCarbonReportSchedule("code-438", "name-393")
        try {
            item.moveTo(CarbonReportScheduleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportScheduleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportSchedule("code-438", "name-393")
        item.status = CarbonReportScheduleStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportScheduleStatus.NEW) }
    }
}
