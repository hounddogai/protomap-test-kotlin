package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaForecast("code-415", "title-121")
        assertEquals(ServiceAreaForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newServiceAreaForecast("code-415", "title-121")
        try {
            item.moveTo(ServiceAreaForecastStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaForecastStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaForecast("code-415", "title-121")
        item.status = ServiceAreaForecastStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaForecastStatus.DRAFT) }
    }
}
