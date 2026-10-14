package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerPreference("code-143", "label-268")
        assertEquals(TrailerPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTrailerPreference("code-143", "label-268")
        try {
            item.moveTo(TrailerPreferenceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerPreferenceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerPreference("code-143", "label-268")
        item.status = TrailerPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerPreferenceStatus.PLANNED) }
    }
}
