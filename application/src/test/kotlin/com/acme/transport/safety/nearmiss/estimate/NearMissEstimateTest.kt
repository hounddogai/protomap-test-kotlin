package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newNearMissEstimate("code-1", "label-674")
        assertEquals(NearMissEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newNearMissEstimate("code-1", "label-674")
        try {
            item.moveTo(NearMissEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNearMissEstimate("code-1", "label-674")
        item.status = NearMissEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(NearMissEstimateStatus.OPEN) }
    }
}
