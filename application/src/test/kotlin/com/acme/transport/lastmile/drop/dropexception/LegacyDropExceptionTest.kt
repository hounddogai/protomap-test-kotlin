package com.acme.transport.lastmile.drop.dropexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDropExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyDropException("code-666", "label-989")
        assertEquals(LegacyDropExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyDropException("code-666", "label-989")
        try {
            item.moveTo(LegacyDropExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDropExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyDropException("code-666", "label-989")
        item.status = LegacyDropExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyDropExceptionStatus.IDLE) }
    }
}
