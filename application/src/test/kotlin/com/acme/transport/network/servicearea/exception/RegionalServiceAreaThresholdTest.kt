package com.acme.transport.network.servicearea.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServiceAreaThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalServiceAreaThreshold("code-929", "name-689")
        assertEquals(RegionalServiceAreaThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalServiceAreaThreshold("code-929", "name-689")
        try {
            item.moveTo(RegionalServiceAreaThresholdStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServiceAreaThresholdStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalServiceAreaThreshold("code-929", "name-689")
        item.status = RegionalServiceAreaThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalServiceAreaThresholdStatus.REQUESTED) }
    }
}
