package com.acme.transport.emissions.evcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientEvChargePreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newLenientEvChargePreference("code-962", "label-387")
        assertEquals(LenientEvChargePreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newLenientEvChargePreference("code-962", "label-387")
        try {
            item.moveTo(LenientEvChargePreferenceStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientEvChargePreferenceStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientEvChargePreference("code-962", "label-387")
        item.status = LenientEvChargePreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(LenientEvChargePreferenceStatus.CREATED) }
    }
}
