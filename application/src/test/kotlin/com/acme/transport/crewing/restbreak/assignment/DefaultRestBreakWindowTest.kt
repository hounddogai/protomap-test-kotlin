package com.acme.transport.crewing.restbreak.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRestBreakWindowTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultRestBreakWindow("code-250", "title-27")
        assertEquals(DefaultRestBreakWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultRestBreakWindow("code-250", "title-27")
        try {
            item.moveTo(DefaultRestBreakWindowStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRestBreakWindowStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultRestBreakWindow("code-250", "title-27")
        item.status = DefaultRestBreakWindowStatus.FAILED
        assertFails("move") { item.moveTo(DefaultRestBreakWindowStatus.NEW) }
    }
}
