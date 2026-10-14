package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBackhaulMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingBackhaulMapping("code-491", "name-568")
        assertEquals(PendingBackhaulMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingBackhaulMapping("code-491", "name-568")
        try {
            item.moveTo(PendingBackhaulMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBackhaulMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingBackhaulMapping("code-491", "name-568")
        item.status = PendingBackhaulMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingBackhaulMappingStatus.REQUESTED) }
    }
}
