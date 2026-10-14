package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalZoneClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalZoneClaim("code-259", "title-692")
        assertEquals(GlobalZoneClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalZoneClaim("code-259", "title-692")
        try {
            item.moveTo(GlobalZoneClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalZoneClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalZoneClaim("code-259", "title-692")
        item.status = GlobalZoneClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalZoneClaimStatus.PLANNED) }
    }
}
