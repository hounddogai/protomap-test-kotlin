package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalParcelProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalParcelProfile("code-247", "label-600")
        assertEquals(GlobalParcelProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalParcelProfile("code-247", "label-600")
        try {
            item.moveTo(GlobalParcelProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalParcelProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalParcelProfile("code-247", "label-600")
        item.status = GlobalParcelProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalParcelProfileStatus.PROPOSED) }
    }
}
