package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultSortationRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultSortationRecord("code-341", "label-438")
        assertEquals(DefaultSortationRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultSortationRecord("code-341", "label-438")
        try {
            item.moveTo(DefaultSortationRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultSortationRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultSortationRecord("code-341", "label-438")
        item.status = DefaultSortationRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultSortationRecordStatus.REQUESTED) }
    }
}
