package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalTrailerPreference("code-825", "title-157")
        assertEquals(RegionalTrailerPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalTrailerPreference("code-825", "title-157")
        try {
            item.moveTo(RegionalTrailerPreferenceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerPreferenceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalTrailerPreference("code-825", "title-157")
        item.status = RegionalTrailerPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalTrailerPreferenceStatus.PLANNED) }
    }
}
