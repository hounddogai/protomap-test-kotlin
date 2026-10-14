package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCrossDockClaimTest {
    @Test
    fun `starts in created`() {
        val item = newTieredCrossDockClaim("code-399", "name-952")
        assertEquals(TieredCrossDockClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTieredCrossDockClaim("code-399", "name-952")
        try {
            item.moveTo(TieredCrossDockClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCrossDockClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredCrossDockClaim("code-399", "name-952")
        item.status = TieredCrossDockClaimStatus.RETIRED
        assertFails("move") { item.moveTo(TieredCrossDockClaimStatus.CREATED) }
    }
}
