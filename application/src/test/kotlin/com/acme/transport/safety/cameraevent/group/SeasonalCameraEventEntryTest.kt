package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCameraEventEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalCameraEventEntry("code-921", "name-578")
        assertEquals(SeasonalCameraEventEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalCameraEventEntry("code-921", "name-578")
        try {
            item.moveTo(SeasonalCameraEventEntryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCameraEventEntryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalCameraEventEntry("code-921", "name-578")
        item.status = SeasonalCameraEventEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalCameraEventEntryStatus.PROPOSED) }
    }
}
