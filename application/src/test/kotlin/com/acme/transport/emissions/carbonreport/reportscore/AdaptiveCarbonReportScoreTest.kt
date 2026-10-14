package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCarbonReportScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveCarbonReportScore("code-427", "name-100")
        assertEquals(AdaptiveCarbonReportScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveCarbonReportScore("code-427", "name-100")
        try {
            item.moveTo(AdaptiveCarbonReportScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCarbonReportScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveCarbonReportScore("code-427", "name-100")
        item.status = AdaptiveCarbonReportScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveCarbonReportScoreStatus.REQUESTED) }
    }
}
