package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTireTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedTireTemplate("code-547", "label-1")
        assertEquals(CappedTireTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCappedTireTemplate("code-547", "label-1")
        try {
            item.moveTo(CappedTireTemplateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTireTemplateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedTireTemplate("code-547", "label-1")
        item.status = CappedTireTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedTireTemplateStatus.PLANNED) }
    }
}
