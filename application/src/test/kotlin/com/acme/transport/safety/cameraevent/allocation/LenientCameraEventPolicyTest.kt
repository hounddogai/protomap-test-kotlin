package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCameraEventPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCameraEventPolicy("code-508", "name-760")
        assertEquals(LenientCameraEventPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientCameraEventPolicy("code-508", "name-760")
        try {
            item.moveTo(LenientCameraEventPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCameraEventPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCameraEventPolicy("code-508", "name-760")
        item.status = LenientCameraEventPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCameraEventPolicyStatus.CREATED) }
    }
}
