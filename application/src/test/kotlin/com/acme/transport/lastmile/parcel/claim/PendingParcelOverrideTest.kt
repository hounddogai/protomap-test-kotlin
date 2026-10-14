package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingParcelOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingParcelOverride("code-210", "name-729")
        assertEquals(PendingParcelOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newPendingParcelOverride("code-210", "name-729")
        try {
            item.moveTo(PendingParcelOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingParcelOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingParcelOverride("code-210", "name-729")
        item.status = PendingParcelOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingParcelOverrideStatus.PROPOSED) }
    }
}
