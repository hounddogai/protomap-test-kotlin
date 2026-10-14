package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newServiceAreaCycle("code-877", "name-252")
        assertEquals(ServiceAreaCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newServiceAreaCycle("code-877", "name-252")
        try {
            item.moveTo(ServiceAreaCycleStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaCycleStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServiceAreaCycle("code-877", "name-252")
        item.status = ServiceAreaCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(ServiceAreaCycleStatus.PLANNED) }
    }
}
