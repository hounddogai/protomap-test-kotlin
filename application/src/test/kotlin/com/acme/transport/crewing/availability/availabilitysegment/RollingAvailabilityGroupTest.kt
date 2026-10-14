package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAvailabilityGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        assertEquals(RollingAvailabilityGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        try {
            item.moveTo(RollingAvailabilityGroupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAvailabilityGroupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        item.status = RollingAvailabilityGroupStatus.CLOSED
        assertFails("move") { item.moveTo(RollingAvailabilityGroupStatus.DRAFT) }
    }
}
