package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServiceAreaDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedServiceAreaDraft("code-130", "label-282")
        assertEquals(CappedServiceAreaDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCappedServiceAreaDraft("code-130", "label-282")
        try {
            item.moveTo(CappedServiceAreaDraftStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServiceAreaDraftStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedServiceAreaDraft("code-130", "label-282")
        item.status = CappedServiceAreaDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedServiceAreaDraftStatus.REQUESTED) }
    }
}
