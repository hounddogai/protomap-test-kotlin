package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRosterExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredRosterException("code-892", "title-114")
        assertEquals(TieredRosterExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTieredRosterException("code-892", "title-114")
        try {
            item.moveTo(TieredRosterExceptionStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRosterExceptionStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredRosterException("code-892", "title-114")
        item.status = TieredRosterExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredRosterExceptionStatus.PLANNED) }
    }
}
