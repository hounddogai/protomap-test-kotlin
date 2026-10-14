package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeEntryTest {
    @Test
    fun `starts in created`() {
        val item = newEvChargeEntry("code-112", "label-500")
        assertEquals(EvChargeEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newEvChargeEntry("code-112", "label-500")
        try {
            item.moveTo(EvChargeEntryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeEntryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEvChargeEntry("code-112", "label-500")
        item.status = EvChargeEntryStatus.RETIRED
        assertFails("move") { item.moveTo(EvChargeEntryStatus.CREATED) }
    }
}
