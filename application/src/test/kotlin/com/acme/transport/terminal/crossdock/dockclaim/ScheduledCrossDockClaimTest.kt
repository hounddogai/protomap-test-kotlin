package com.acme.transport.terminal.crossdock.dockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCrossDockClaimTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledCrossDockClaim("code-721", "name-732")
        assertEquals(ScheduledCrossDockClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledCrossDockClaim("code-721", "name-732")
        try {
            item.moveTo(ScheduledCrossDockClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCrossDockClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledCrossDockClaim("code-721", "name-732")
        item.status = ScheduledCrossDockClaimStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledCrossDockClaimStatus.CREATED) }
    }
}
