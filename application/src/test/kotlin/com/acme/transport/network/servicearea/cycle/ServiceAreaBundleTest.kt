package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaBundle("code-757", "title-618")
        assertEquals(ServiceAreaBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newServiceAreaBundle("code-757", "title-618")
        try {
            item.moveTo(ServiceAreaBundleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaBundleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaBundle("code-757", "title-618")
        item.status = ServiceAreaBundleStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaBundleStatus.IDLE) }
    }
}
