package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCrossDockRecordTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryCrossDockRecord("code-229", "label-977")
        assertEquals(PrimaryCrossDockRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPrimaryCrossDockRecord("code-229", "label-977")
        try {
            item.moveTo(PrimaryCrossDockRecordStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCrossDockRecordStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryCrossDockRecord("code-229", "label-977")
        item.status = PrimaryCrossDockRecordStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryCrossDockRecordStatus.NEW) }
    }
}
