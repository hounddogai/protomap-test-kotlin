package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredUtilizationProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredUtilizationProfile("code-637", "name-317")
        assertEquals(TieredUtilizationProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredUtilizationProfile("code-637", "name-317")
        try {
            item.moveTo(TieredUtilizationProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredUtilizationProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredUtilizationProfile("code-637", "name-317")
        item.status = TieredUtilizationProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredUtilizationProfileStatus.PLANNED) }
    }
}
