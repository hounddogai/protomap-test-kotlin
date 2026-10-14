package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServiceAreaThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalServiceAreaThreshold("code-625", "name-928")
        assertEquals(SeasonalServiceAreaThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalServiceAreaThreshold("code-625", "name-928")
        try {
            item.moveTo(SeasonalServiceAreaThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServiceAreaThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalServiceAreaThreshold("code-625", "name-928")
        item.status = SeasonalServiceAreaThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalServiceAreaThresholdStatus.CREATED) }
    }
}
