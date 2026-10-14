package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedParcelForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedParcelForecast("code-448", "title-528")
        assertEquals(WeightedParcelForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedParcelForecast("code-448", "title-528")
        try {
            item.moveTo(WeightedParcelForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedParcelForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedParcelForecast("code-448", "title-528")
        item.status = WeightedParcelForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedParcelForecastStatus.PROPOSED) }
    }
}
