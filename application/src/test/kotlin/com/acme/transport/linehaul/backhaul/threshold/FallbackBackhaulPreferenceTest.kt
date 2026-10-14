package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBackhaulPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackBackhaulPreference("code-889", "name-221")
        assertEquals(FallbackBackhaulPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackBackhaulPreference("code-889", "name-221")
        try {
            item.moveTo(FallbackBackhaulPreferenceStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBackhaulPreferenceStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackBackhaulPreference("code-889", "name-221")
        item.status = FallbackBackhaulPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(FallbackBackhaulPreferenceStatus.NEW) }
    }
}
