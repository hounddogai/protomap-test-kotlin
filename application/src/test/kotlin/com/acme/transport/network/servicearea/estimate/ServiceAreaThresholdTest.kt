package com.acme.transport.network.servicearea.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaThreshold("code-823", "name-768")
        assertEquals(ServiceAreaThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newServiceAreaThreshold("code-823", "name-768")
        try {
            item.moveTo(ServiceAreaThresholdStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaThresholdStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaThreshold("code-823", "name-768")
        item.status = ServiceAreaThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaThresholdStatus.DRAFT) }
    }
}
