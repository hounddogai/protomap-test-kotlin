package com.acme.transport.emissions.idling.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingBaseline("code-41", "label-368")
        assertEquals(IdlingBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newIdlingBaseline("code-41", "label-368")
        try {
            item.moveTo(IdlingBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingBaseline("code-41", "label-368")
        item.status = IdlingBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingBaselineStatus.DRAFT) }
    }
}
