package com.acme.transport.lastmile.drop.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropProfileTest {
    @Test
    fun `starts in new`() {
        val item = newDropProfile("code-614", "label-632")
        assertEquals(DropProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDropProfile("code-614", "label-632")
        try {
            item.moveTo(DropProfileStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropProfileStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDropProfile("code-614", "label-632")
        item.status = DropProfileStatus.FAILED
        assertFails("move") { item.moveTo(DropProfileStatus.NEW) }
    }
}
