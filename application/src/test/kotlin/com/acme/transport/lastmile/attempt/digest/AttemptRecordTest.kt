package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newAttemptRecord("code-273", "name-341")
        assertEquals(AttemptRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAttemptRecord("code-273", "name-341")
        try {
            item.moveTo(AttemptRecordStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptRecordStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAttemptRecord("code-273", "name-341")
        item.status = AttemptRecordStatus.STOPPED
        assertFails("move") { item.moveTo(AttemptRecordStatus.IDLE) }
    }
}
