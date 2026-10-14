package com.acme.transport.safety.coaching.coachingtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCoachingForecastTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCoachingForecast("code-617", "title-371")
        assertEquals(LenientCoachingForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientCoachingForecast("code-617", "title-371")
        try {
            item.moveTo(LenientCoachingForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCoachingForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCoachingForecast("code-617", "title-371")
        item.status = LenientCoachingForecastStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCoachingForecastStatus.CREATED) }
    }
}
