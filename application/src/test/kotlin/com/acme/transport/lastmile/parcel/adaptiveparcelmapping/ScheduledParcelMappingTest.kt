package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledParcelMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledParcelMapping("code-722", "title-672")
        assertEquals(ScheduledParcelMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledParcelMapping("code-722", "title-672")
        try {
            item.moveTo(ScheduledParcelMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledParcelMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledParcelMapping("code-722", "title-672")
        item.status = ScheduledParcelMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledParcelMappingStatus.REQUESTED) }
    }
}
