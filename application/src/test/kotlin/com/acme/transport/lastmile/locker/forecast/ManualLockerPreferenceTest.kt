package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLockerPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newManualLockerPreference("code-361", "title-75")
        assertEquals(ManualLockerPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualLockerPreference("code-361", "title-75")
        try {
            item.moveTo(ManualLockerPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLockerPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualLockerPreference("code-361", "title-75")
        item.status = ManualLockerPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualLockerPreferenceStatus.PLANNED) }
    }
}
