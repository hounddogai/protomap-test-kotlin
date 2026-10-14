package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBackhaulPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyBackhaulPreference("code-770", "label-718")
        assertEquals(LegacyBackhaulPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyBackhaulPreference("code-770", "label-718")
        try {
            item.moveTo(LegacyBackhaulPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBackhaulPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyBackhaulPreference("code-770", "label-718")
        item.status = LegacyBackhaulPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyBackhaulPreferenceStatus.REQUESTED) }
    }
}
