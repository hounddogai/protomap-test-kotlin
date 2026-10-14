package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedZoneClaimTest {
    @Test
    fun `starts in created`() {
        val item = newCappedZoneClaim("code-767", "name-127")
        assertEquals(CappedZoneClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedZoneClaim("code-767", "name-127")
        try {
            item.moveTo(CappedZoneClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedZoneClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedZoneClaim("code-767", "name-127")
        item.status = CappedZoneClaimStatus.RETIRED
        assertFails("move") { item.moveTo(CappedZoneClaimStatus.CREATED) }
    }
}
