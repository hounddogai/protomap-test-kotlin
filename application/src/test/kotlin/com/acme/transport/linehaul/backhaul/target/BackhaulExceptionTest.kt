package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newBackhaulException("code-523", "label-686")
        assertEquals(BackhaulExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBackhaulException("code-523", "label-686")
        try {
            item.moveTo(BackhaulExceptionStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulExceptionStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBackhaulException("code-523", "label-686")
        item.status = BackhaulExceptionStatus.FAILED
        assertFails("move") { item.moveTo(BackhaulExceptionStatus.NEW) }
    }
}
