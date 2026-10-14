package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIncidentPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newTieredIncidentPreference("code-164", "label-497")
        assertEquals(TieredIncidentPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredIncidentPreference("code-164", "label-497")
        try {
            item.moveTo(TieredIncidentPreferenceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIncidentPreferenceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredIncidentPreference("code-164", "label-497")
        item.status = TieredIncidentPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(TieredIncidentPreferenceStatus.NEW) }
    }
}
