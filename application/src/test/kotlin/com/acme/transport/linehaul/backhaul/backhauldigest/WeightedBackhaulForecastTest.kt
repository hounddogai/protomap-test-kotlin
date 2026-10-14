package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBackhaulForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        assertEquals(WeightedBackhaulForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        try {
            item.moveTo(WeightedBackhaulForecastStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBackhaulForecastStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        item.status = WeightedBackhaulForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedBackhaulForecastStatus.REQUESTED) }
    }
}
