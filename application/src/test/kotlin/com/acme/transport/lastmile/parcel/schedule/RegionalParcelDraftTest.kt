package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalParcelDraft("code-96", "label-415")
        assertEquals(RegionalParcelDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalParcelDraft("code-96", "label-415")
        try {
            item.moveTo(RegionalParcelDraftStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelDraftStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalParcelDraft("code-96", "label-415")
        item.status = RegionalParcelDraftStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalParcelDraftStatus.IDLE) }
    }
}
