package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAccessorialCycleTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        assertEquals(DefaultAccessorialCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        try {
            item.moveTo(DefaultAccessorialCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAccessorialCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultAccessorialCycle("code-693", "name-541")
        item.status = DefaultAccessorialCycleStatus.FAILED
        assertFails("move") { item.moveTo(DefaultAccessorialCycleStatus.NEW) }
    }
}
