package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalRedeliveryThreshold("code-928", "name-907")
        assertEquals(SeasonalRedeliveryThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalRedeliveryThreshold("code-928", "name-907")
        try {
            item.moveTo(SeasonalRedeliveryThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalRedeliveryThreshold("code-928", "name-907")
        item.status = SeasonalRedeliveryThresholdStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalRedeliveryThresholdStatus.NEW) }
    }
}
