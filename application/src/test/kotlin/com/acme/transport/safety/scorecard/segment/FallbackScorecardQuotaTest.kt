package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackScorecardQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackScorecardQuota("code-932", "label-587")
        assertEquals(FallbackScorecardQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newFallbackScorecardQuota("code-932", "label-587")
        try {
            item.moveTo(FallbackScorecardQuotaStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackScorecardQuotaStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackScorecardQuota("code-932", "label-587")
        item.status = FallbackScorecardQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackScorecardQuotaStatus.IDLE) }
    }
}
