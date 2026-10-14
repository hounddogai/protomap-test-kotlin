package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropRecordTest {
    @Test
    fun `starts in new`() {
        val item = newDropRecord("code-432", "label-916")
        assertEquals(DropRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDropRecord("code-432", "label-916")
        try {
            item.moveTo(DropRecordStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropRecordStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDropRecord("code-432", "label-916")
        item.status = DropRecordStatus.FAILED
        assertFails("move") { item.moveTo(DropRecordStatus.NEW) }
    }
}
