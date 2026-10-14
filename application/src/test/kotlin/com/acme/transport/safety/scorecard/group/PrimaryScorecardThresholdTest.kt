package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryScorecardThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        assertEquals(PrimaryScorecardThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        try {
            item.moveTo(PrimaryScorecardThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryScorecardThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        item.status = PrimaryScorecardThresholdStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryScorecardThresholdStatus.NEW) }
    }
}
