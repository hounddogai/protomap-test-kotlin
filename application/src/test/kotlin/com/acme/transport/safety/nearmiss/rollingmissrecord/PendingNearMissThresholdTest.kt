package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNearMissThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newPendingNearMissThreshold("code-321", "name-904")
        assertEquals(PendingNearMissThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingNearMissThreshold("code-321", "name-904")
        try {
            item.moveTo(PendingNearMissThresholdStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNearMissThresholdStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingNearMissThreshold("code-321", "name-904")
        item.status = PendingNearMissThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(PendingNearMissThresholdStatus.CREATED) }
    }
}
