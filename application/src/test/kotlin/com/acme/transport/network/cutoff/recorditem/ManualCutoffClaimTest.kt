package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCutoffClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualCutoffClaim("code-894", "label-92")
        assertEquals(ManualCutoffClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newManualCutoffClaim("code-894", "label-92")
        try {
            item.moveTo(ManualCutoffClaimStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCutoffClaimStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualCutoffClaim("code-894", "label-92")
        item.status = ManualCutoffClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualCutoffClaimStatus.PROPOSED) }
    }
}
