package com.acme.transport.network.servicearea.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newServiceAreaOverride("code-939", "title-685")
        assertEquals(ServiceAreaOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newServiceAreaOverride("code-939", "title-685")
        try {
            item.moveTo(ServiceAreaOverrideStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaOverrideStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServiceAreaOverride("code-939", "title-685")
        item.status = ServiceAreaOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(ServiceAreaOverrideStatus.PLANNED) }
    }
}
