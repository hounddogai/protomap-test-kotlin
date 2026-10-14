package com.acme.transport.fleetops.tire.tirewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTireCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingTireCycle("code-477", "title-277")
        assertEquals(RollingTireCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingTireCycle("code-477", "title-277")
        try {
            item.moveTo(RollingTireCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTireCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingTireCycle("code-477", "title-277")
        item.status = RollingTireCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingTireCycleStatus.PLANNED) }
    }
}
