package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalParcelDraftTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalParcelDraft("code-406", "name-504")
        assertEquals(GlobalParcelDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newGlobalParcelDraft("code-406", "name-504")
        try {
            item.moveTo(GlobalParcelDraftStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalParcelDraftStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalParcelDraft("code-406", "name-504")
        item.status = GlobalParcelDraftStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalParcelDraftStatus.CREATED) }
    }
}
