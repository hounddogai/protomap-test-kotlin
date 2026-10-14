package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newUtilizationProfile("code-839", "label-222")
        assertEquals(UtilizationProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newUtilizationProfile("code-839", "label-222")
        try {
            item.moveTo(UtilizationProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newUtilizationProfile("code-839", "label-222")
        item.status = UtilizationProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(UtilizationProfileStatus.PROPOSED) }
    }
}
