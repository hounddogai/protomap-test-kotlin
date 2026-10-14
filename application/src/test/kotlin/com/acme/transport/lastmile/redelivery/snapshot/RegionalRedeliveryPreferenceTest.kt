package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRedeliveryPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalRedeliveryPreference("code-28", "name-594")
        assertEquals(RegionalRedeliveryPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalRedeliveryPreference("code-28", "name-594")
        try {
            item.moveTo(RegionalRedeliveryPreferenceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRedeliveryPreferenceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalRedeliveryPreference("code-28", "name-594")
        item.status = RegionalRedeliveryPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(RegionalRedeliveryPreferenceStatus.NEW) }
    }
}
