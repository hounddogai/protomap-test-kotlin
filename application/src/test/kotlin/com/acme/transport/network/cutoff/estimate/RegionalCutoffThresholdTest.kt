package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCutoffThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalCutoffThreshold("code-769", "label-409")
        assertEquals(RegionalCutoffThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalCutoffThreshold("code-769", "label-409")
        try {
            item.moveTo(RegionalCutoffThresholdStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCutoffThresholdStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalCutoffThreshold("code-769", "label-409")
        item.status = RegionalCutoffThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalCutoffThresholdStatus.REQUESTED) }
    }
}
