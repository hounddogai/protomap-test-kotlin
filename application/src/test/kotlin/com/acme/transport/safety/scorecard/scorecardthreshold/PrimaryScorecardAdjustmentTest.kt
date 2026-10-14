package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryScorecardAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        assertEquals(PrimaryScorecardAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        try {
            item.moveTo(PrimaryScorecardAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryScorecardAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        item.status = PrimaryScorecardAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryScorecardAdjustmentStatus.NEW) }
    }
}
