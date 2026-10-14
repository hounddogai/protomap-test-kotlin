package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTariffScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictTariffScore("code-97", "title-64")
        assertEquals(StrictTariffScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictTariffScore("code-97", "title-64")
        try {
            item.moveTo(StrictTariffScoreStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTariffScoreStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictTariffScore("code-97", "title-64")
        item.status = StrictTariffScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictTariffScoreStatus.PLANNED) }
    }
}
