package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledIdlingWindowTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledIdlingWindow("code-129", "name-134")
        assertEquals(ScheduledIdlingWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledIdlingWindow("code-129", "name-134")
        try {
            item.moveTo(ScheduledIdlingWindowStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledIdlingWindowStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledIdlingWindow("code-129", "name-134")
        item.status = ScheduledIdlingWindowStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledIdlingWindowStatus.NEW) }
    }
}
