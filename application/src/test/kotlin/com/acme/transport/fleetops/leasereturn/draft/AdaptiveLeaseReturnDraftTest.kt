package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLeaseReturnDraftTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveLeaseReturnDraft("code-747", "name-98")
        assertEquals(AdaptiveLeaseReturnDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveLeaseReturnDraft("code-747", "name-98")
        try {
            item.moveTo(AdaptiveLeaseReturnDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLeaseReturnDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveLeaseReturnDraft("code-747", "name-98")
        item.status = AdaptiveLeaseReturnDraftStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveLeaseReturnDraftStatus.CREATED) }
    }
}
