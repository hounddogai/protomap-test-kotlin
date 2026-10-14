package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingWindow("code-520", "title-166")
        assertEquals(IdlingWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIdlingWindow("code-520", "title-166")
        try {
            item.moveTo(IdlingWindowStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingWindowStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingWindow("code-520", "title-166")
        item.status = IdlingWindowStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingWindowStatus.DRAFT) }
    }
}
