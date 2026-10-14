package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newServiceAreaSlot("code-891", "label-94")
        assertEquals(ServiceAreaSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newServiceAreaSlot("code-891", "label-94")
        try {
            item.moveTo(ServiceAreaSlotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaSlotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServiceAreaSlot("code-891", "label-94")
        item.status = ServiceAreaSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(ServiceAreaSlotStatus.REQUESTED) }
    }
}
