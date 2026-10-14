package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffScoreTest {
    @Test
    fun `starts in created`() {
        val item = newTariffScore("code-829", "label-983")
        assertEquals(TariffScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTariffScore("code-829", "label-983")
        try {
            item.moveTo(TariffScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTariffScore("code-829", "label-983")
        item.status = TariffScoreStatus.RETIRED
        assertFails("move") { item.moveTo(TariffScoreStatus.CREATED) }
    }
}
