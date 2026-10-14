package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDockDoorExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedDockDoorException("code-816", "title-244")
        assertEquals(WeightedDockDoorExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedDockDoorException("code-816", "title-244")
        try {
            item.moveTo(WeightedDockDoorExceptionStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDockDoorExceptionStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedDockDoorException("code-816", "title-244")
        item.status = WeightedDockDoorExceptionStatus.FAILED
        assertFails("move") { item.moveTo(WeightedDockDoorExceptionStatus.NEW) }
    }
}
