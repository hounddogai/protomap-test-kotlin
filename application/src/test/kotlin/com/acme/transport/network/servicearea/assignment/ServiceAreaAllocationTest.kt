package com.acme.transport.network.servicearea.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaAllocation("code-181", "name-421")
        assertEquals(ServiceAreaAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newServiceAreaAllocation("code-181", "name-421")
        try {
            item.moveTo(ServiceAreaAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaAllocation("code-181", "name-421")
        item.status = ServiceAreaAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaAllocationStatus.DRAFT) }
    }
}
