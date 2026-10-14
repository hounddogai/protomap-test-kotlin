package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newTireTarget("code-222", "label-709")
        assertEquals(TireTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTireTarget("code-222", "label-709")
        try {
            item.moveTo(TireTargetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireTargetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTireTarget("code-222", "label-709")
        item.status = TireTargetStatus.CLOSED
        assertFails("move") { item.moveTo(TireTargetStatus.DRAFT) }
    }
}
