package com.acme.transport.safety.cameraevent.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCameraEventAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCameraEventAdjustment("code-56", "label-801")
        assertEquals(LenientCameraEventAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientCameraEventAdjustment("code-56", "label-801")
        try {
            item.moveTo(LenientCameraEventAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCameraEventAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCameraEventAdjustment("code-56", "label-801")
        item.status = LenientCameraEventAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCameraEventAdjustmentStatus.CREATED) }
    }
}
