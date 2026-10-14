package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeGroupTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeGroup("code-507", "title-132")
        assertEquals(EvChargeGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newEvChargeGroup("code-507", "title-132")
        try {
            item.moveTo(EvChargeGroupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeGroupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeGroup("code-507", "title-132")
        item.status = EvChargeGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeGroupStatus.OPEN) }
    }
}
