package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDockDoorClaimTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalDockDoorClaim("code-756", "name-817")
        assertEquals(RegionalDockDoorClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalDockDoorClaim("code-756", "name-817")
        try {
            item.moveTo(RegionalDockDoorClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDockDoorClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalDockDoorClaim("code-756", "name-817")
        item.status = RegionalDockDoorClaimStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalDockDoorClaimStatus.CREATED) }
    }
}
