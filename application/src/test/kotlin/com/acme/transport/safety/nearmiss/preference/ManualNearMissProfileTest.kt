package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualNearMissProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newManualNearMissProfile("code-684", "name-555")
        assertEquals(ManualNearMissProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualNearMissProfile("code-684", "name-555")
        try {
            item.moveTo(ManualNearMissProfileStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualNearMissProfileStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualNearMissProfile("code-684", "name-555")
        item.status = ManualNearMissProfileStatus.STOPPED
        assertFails("move") { item.moveTo(ManualNearMissProfileStatus.IDLE) }
    }
}
