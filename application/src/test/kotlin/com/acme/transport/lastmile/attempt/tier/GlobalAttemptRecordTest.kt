package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAttemptRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalAttemptRecord("code-926", "label-728")
        assertEquals(GlobalAttemptRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalAttemptRecord("code-926", "label-728")
        try {
            item.moveTo(GlobalAttemptRecordStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAttemptRecordStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalAttemptRecord("code-926", "label-728")
        item.status = GlobalAttemptRecordStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalAttemptRecordStatus.IDLE) }
    }
}
