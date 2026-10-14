package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRedeliveryEntryTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        assertEquals(RegionalRedeliveryEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        try {
            item.moveTo(RegionalRedeliveryEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRedeliveryEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalRedeliveryEntry("code-172", "label-76")
        item.status = RegionalRedeliveryEntryStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalRedeliveryEntryStatus.CREATED) }
    }
}
