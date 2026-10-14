package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newIdlingPreference("code-745", "title-357")
        assertEquals(IdlingPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newIdlingPreference("code-745", "title-357")
        try {
            item.moveTo(IdlingPreferenceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingPreferenceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIdlingPreference("code-745", "title-357")
        item.status = IdlingPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(IdlingPreferenceStatus.PLANNED) }
    }
}
