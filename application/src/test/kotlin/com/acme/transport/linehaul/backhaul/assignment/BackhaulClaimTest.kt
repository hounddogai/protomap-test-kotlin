package com.acme.transport.linehaul.backhaul.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulClaimTest {
    @Test
    fun `starts in created`() {
        val item = newBackhaulClaim("code-751", "name-191")
        assertEquals(BackhaulClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBackhaulClaim("code-751", "name-191")
        try {
            item.moveTo(BackhaulClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBackhaulClaim("code-751", "name-191")
        item.status = BackhaulClaimStatus.RETIRED
        assertFails("move") { item.moveTo(BackhaulClaimStatus.CREATED) }
    }
}
