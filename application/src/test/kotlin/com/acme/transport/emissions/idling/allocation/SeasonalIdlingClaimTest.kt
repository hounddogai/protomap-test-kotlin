package com.acme.transport.emissions.idling.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalIdlingClaimTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalIdlingClaim("code-570", "label-170")
        assertEquals(SeasonalIdlingClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalIdlingClaim("code-570", "label-170")
        try {
            item.moveTo(SeasonalIdlingClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalIdlingClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalIdlingClaim("code-570", "label-170")
        item.status = SeasonalIdlingClaimStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalIdlingClaimStatus.CREATED) }
    }
}
