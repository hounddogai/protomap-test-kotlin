package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellProfileTest {
    @Test
    fun `starts in new`() {
        val item = newManualDwellProfile("code-437", "title-133")
        assertEquals(ManualDwellProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualDwellProfile("code-437", "title-133")
        try {
            item.moveTo(ManualDwellProfileStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellProfileStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualDwellProfile("code-437", "title-133")
        item.status = ManualDwellProfileStatus.FAILED
        assertFails("move") { item.moveTo(ManualDwellProfileStatus.NEW) }
    }
}
