package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServiceAreaCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictServiceAreaCycle("code-793", "title-379")
        assertEquals(StrictServiceAreaCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictServiceAreaCycle("code-793", "title-379")
        try {
            item.moveTo(StrictServiceAreaCycleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServiceAreaCycleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictServiceAreaCycle("code-793", "title-379")
        item.status = StrictServiceAreaCycleStatus.STOPPED
        assertFails("move") { item.moveTo(StrictServiceAreaCycleStatus.IDLE) }
    }
}
