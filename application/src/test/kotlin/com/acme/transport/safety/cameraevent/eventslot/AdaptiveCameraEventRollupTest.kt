package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCameraEventRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveCameraEventRollup("code-199", "name-728")
        assertEquals(AdaptiveCameraEventRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveCameraEventRollup("code-199", "name-728")
        try {
            item.moveTo(AdaptiveCameraEventRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCameraEventRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveCameraEventRollup("code-199", "name-728")
        item.status = AdaptiveCameraEventRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveCameraEventRollupStatus.PLANNED) }
    }
}
