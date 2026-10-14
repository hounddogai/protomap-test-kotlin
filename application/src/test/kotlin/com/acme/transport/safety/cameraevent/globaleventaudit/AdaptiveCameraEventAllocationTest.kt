package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCameraEventAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        assertEquals(AdaptiveCameraEventAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        try {
            item.moveTo(AdaptiveCameraEventAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCameraEventAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        item.status = AdaptiveCameraEventAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveCameraEventAllocationStatus.REQUESTED) }
    }
}
