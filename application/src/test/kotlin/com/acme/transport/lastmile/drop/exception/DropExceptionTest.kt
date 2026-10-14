package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newDropException("code-689", "name-947")
        assertEquals(DropExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDropException("code-689", "name-947")
        try {
            item.moveTo(DropExceptionStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropExceptionStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDropException("code-689", "name-947")
        item.status = DropExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(DropExceptionStatus.OPEN) }
    }
}
