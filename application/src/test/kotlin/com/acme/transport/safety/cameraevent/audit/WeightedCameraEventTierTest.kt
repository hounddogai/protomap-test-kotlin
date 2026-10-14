package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCameraEventTierTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedCameraEventTier("code-935", "name-471")
        assertEquals(WeightedCameraEventTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newWeightedCameraEventTier("code-935", "name-471")
        try {
            item.moveTo(WeightedCameraEventTierStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCameraEventTierStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedCameraEventTier("code-935", "name-471")
        item.status = WeightedCameraEventTierStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedCameraEventTierStatus.REQUESTED) }
    }
}
