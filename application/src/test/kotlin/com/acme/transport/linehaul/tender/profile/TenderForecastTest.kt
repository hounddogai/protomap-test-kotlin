package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderForecastTest {
    @Test
    fun `starts in created`() {
        val item = newTenderForecast("code-125", "title-537")
        assertEquals(TenderForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTenderForecast("code-125", "title-537")
        try {
            item.moveTo(TenderForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderForecast("code-125", "title-537")
        item.status = TenderForecastStatus.RETIRED
        assertFails("move") { item.moveTo(TenderForecastStatus.CREATED) }
    }
}
