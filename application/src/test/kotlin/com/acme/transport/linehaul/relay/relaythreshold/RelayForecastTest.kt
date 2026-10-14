package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayForecastTest {
    @Test
    fun `starts in created`() {
        val item = newRelayForecast("code-948", "label-618")
        assertEquals(RelayForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRelayForecast("code-948", "label-618")
        try {
            item.moveTo(RelayForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRelayForecast("code-948", "label-618")
        item.status = RelayForecastStatus.RETIRED
        assertFails("move") { item.moveTo(RelayForecastStatus.CREATED) }
    }
}
