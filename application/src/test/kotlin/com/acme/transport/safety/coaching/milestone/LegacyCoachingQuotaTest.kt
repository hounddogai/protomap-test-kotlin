package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCoachingQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyCoachingQuota("code-719", "label-768")
        assertEquals(LegacyCoachingQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyCoachingQuota("code-719", "label-768")
        try {
            item.moveTo(LegacyCoachingQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCoachingQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyCoachingQuota("code-719", "label-768")
        item.status = LegacyCoachingQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyCoachingQuotaStatus.IDLE) }
    }
}
