package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCrossDockThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalCrossDockThreshold("code-662", "name-409")
        assertEquals(SeasonalCrossDockThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalCrossDockThreshold("code-662", "name-409")
        try {
            item.moveTo(SeasonalCrossDockThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCrossDockThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalCrossDockThreshold("code-662", "name-409")
        item.status = SeasonalCrossDockThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalCrossDockThresholdStatus.DRAFT) }
    }
}
