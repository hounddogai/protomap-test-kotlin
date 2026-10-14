package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCapacityPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackCapacityPreference("code-466", "title-820")
        assertEquals(FallbackCapacityPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFallbackCapacityPreference("code-466", "title-820")
        try {
            item.moveTo(FallbackCapacityPreferenceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCapacityPreferenceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackCapacityPreference("code-466", "title-820")
        item.status = FallbackCapacityPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackCapacityPreferenceStatus.OPEN) }
    }
}
