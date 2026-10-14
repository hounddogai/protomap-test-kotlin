package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newUtilizationThreshold("code-700", "name-757")
        assertEquals(UtilizationThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newUtilizationThreshold("code-700", "name-757")
        try {
            item.moveTo(UtilizationThresholdStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationThresholdStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newUtilizationThreshold("code-700", "name-757")
        item.status = UtilizationThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(UtilizationThresholdStatus.PROPOSED) }
    }
}
