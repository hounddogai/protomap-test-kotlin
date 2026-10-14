package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEvChargeGroupTest {
    @Test
    fun `starts in new`() {
        val item = newTieredEvChargeGroup("code-671", "title-473")
        assertEquals(TieredEvChargeGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTieredEvChargeGroup("code-671", "title-473")
        try {
            item.moveTo(TieredEvChargeGroupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEvChargeGroupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredEvChargeGroup("code-671", "title-473")
        item.status = TieredEvChargeGroupStatus.FAILED
        assertFails("move") { item.moveTo(TieredEvChargeGroupStatus.NEW) }
    }
}
