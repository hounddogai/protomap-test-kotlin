package com.acme.transport.safety.nearmiss.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newNearMissPreference("code-185", "label-881")
        assertEquals(NearMissPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNearMissPreference("code-185", "label-881")
        try {
            item.moveTo(NearMissPreferenceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissPreferenceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNearMissPreference("code-185", "label-881")
        item.status = NearMissPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(NearMissPreferenceStatus.PLANNED) }
    }
}
