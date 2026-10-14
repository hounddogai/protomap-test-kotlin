package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newManualDwellOverride("code-695", "name-99")
        assertEquals(ManualDwellOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualDwellOverride("code-695", "name-99")
        try {
            item.moveTo(ManualDwellOverrideStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellOverrideStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualDwellOverride("code-695", "name-99")
        item.status = ManualDwellOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(ManualDwellOverrideStatus.IDLE) }
    }
}
