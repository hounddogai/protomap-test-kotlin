package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newCutoffPreference("code-442", "title-77")
        assertEquals(CutoffPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCutoffPreference("code-442", "title-77")
        try {
            item.moveTo(CutoffPreferenceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffPreferenceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCutoffPreference("code-442", "title-77")
        item.status = CutoffPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(CutoffPreferenceStatus.PLANNED) }
    }
}
