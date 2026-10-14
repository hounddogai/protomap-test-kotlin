package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTransitTimePreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveTransitTimePreference("code-664", "label-890")
        assertEquals(AdaptiveTransitTimePreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveTransitTimePreference("code-664", "label-890")
        try {
            item.moveTo(AdaptiveTransitTimePreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTransitTimePreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveTransitTimePreference("code-664", "label-890")
        item.status = AdaptiveTransitTimePreferenceStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveTransitTimePreferenceStatus.NEW) }
    }
}
