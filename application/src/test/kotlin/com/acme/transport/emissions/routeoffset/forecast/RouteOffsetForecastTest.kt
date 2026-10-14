package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newRouteOffsetForecast("code-291", "name-274")
        assertEquals(RouteOffsetForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRouteOffsetForecast("code-291", "name-274")
        try {
            item.moveTo(RouteOffsetForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRouteOffsetForecast("code-291", "name-274")
        item.status = RouteOffsetForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(RouteOffsetForecastStatus.PLANNED) }
    }
}
