package com.acme.transport.fleetops.serviceplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServicePlanPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        assertEquals(SeasonalServicePlanPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        try {
            item.moveTo(SeasonalServicePlanPreferenceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServicePlanPreferenceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        item.status = SeasonalServicePlanPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalServicePlanPreferenceStatus.PROPOSED) }
    }
}
