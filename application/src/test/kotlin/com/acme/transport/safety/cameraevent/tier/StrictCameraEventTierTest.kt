package com.acme.transport.safety.cameraevent.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCameraEventTierTest {
    @Test
    fun `starts in new`() {
        val item = newStrictCameraEventTier("code-511", "name-110")
        assertEquals(StrictCameraEventTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictCameraEventTier("code-511", "name-110")
        try {
            item.moveTo(StrictCameraEventTierStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCameraEventTierStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictCameraEventTier("code-511", "name-110")
        item.status = StrictCameraEventTierStatus.FAILED
        assertFails("move") { item.moveTo(StrictCameraEventTierStatus.NEW) }
    }
}
