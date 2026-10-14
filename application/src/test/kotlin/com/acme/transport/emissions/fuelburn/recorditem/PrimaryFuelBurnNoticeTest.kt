package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelBurnNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryFuelBurnNotice("code-51", "name-823")
        assertEquals(PrimaryFuelBurnNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryFuelBurnNotice("code-51", "name-823")
        try {
            item.moveTo(PrimaryFuelBurnNoticeStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelBurnNoticeStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryFuelBurnNotice("code-51", "name-823")
        item.status = PrimaryFuelBurnNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryFuelBurnNoticeStatus.PROPOSED) }
    }
}
