package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingScore("code-277", "label-43")
        assertEquals(IdlingScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIdlingScore("code-277", "label-43")
        try {
            item.moveTo(IdlingScoreStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingScoreStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingScore("code-277", "label-43")
        item.status = IdlingScoreStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingScoreStatus.DRAFT) }
    }
}
