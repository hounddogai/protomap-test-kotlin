package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newManualCameraEventPolicy("code-809", "name-433")
        assertEquals(ManualCameraEventPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualCameraEventPolicy("code-809", "name-433")
        try {
            item.moveTo(ManualCameraEventPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualCameraEventPolicy("code-809", "name-433")
        item.status = ManualCameraEventPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(ManualCameraEventPolicyStatus.CREATED) }
    }
}
