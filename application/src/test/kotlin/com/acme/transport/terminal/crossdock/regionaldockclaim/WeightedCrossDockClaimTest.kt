package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCrossDockClaimTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        assertEquals(WeightedCrossDockClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        try {
            item.moveTo(WeightedCrossDockClaimStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCrossDockClaimStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        item.status = WeightedCrossDockClaimStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedCrossDockClaimStatus.CREATED) }
    }
}
