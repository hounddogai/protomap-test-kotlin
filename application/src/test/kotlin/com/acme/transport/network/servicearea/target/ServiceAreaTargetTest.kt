package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaTarget("code-202", "title-656")
        assertEquals(ServiceAreaTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newServiceAreaTarget("code-202", "title-656")
        try {
            item.moveTo(ServiceAreaTargetStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaTargetStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaTarget("code-202", "title-656")
        item.status = ServiceAreaTargetStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaTargetStatus.IDLE) }
    }
}
