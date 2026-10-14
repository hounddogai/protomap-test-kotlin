package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServiceAreaClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualServiceAreaClaim("code-774", "name-645")
        assertEquals(ManualServiceAreaClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualServiceAreaClaim("code-774", "name-645")
        try {
            item.moveTo(ManualServiceAreaClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServiceAreaClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualServiceAreaClaim("code-774", "name-645")
        item.status = ManualServiceAreaClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualServiceAreaClaimStatus.PROPOSED) }
    }
}
