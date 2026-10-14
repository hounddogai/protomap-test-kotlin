package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newFuelBurnNotice("code-726", "label-402")
        assertEquals(FuelBurnNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFuelBurnNotice("code-726", "label-402")
        try {
            item.moveTo(FuelBurnNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFuelBurnNotice("code-726", "label-402")
        item.status = FuelBurnNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FuelBurnNoticeStatus.PROPOSED) }
    }
}
