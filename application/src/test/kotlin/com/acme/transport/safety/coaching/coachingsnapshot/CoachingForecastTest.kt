package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newCoachingForecast("code-737", "title-634")
        assertEquals(CoachingForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCoachingForecast("code-737", "title-634")
        try {
            item.moveTo(CoachingForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCoachingForecast("code-737", "title-634")
        item.status = CoachingForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CoachingForecastStatus.PROPOSED) }
    }
}
