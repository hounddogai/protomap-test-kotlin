package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaDraft("code-350", "name-322")
        assertEquals(ServiceAreaDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newServiceAreaDraft("code-350", "name-322")
        try {
            item.moveTo(ServiceAreaDraftStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaDraftStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaDraft("code-350", "name-322")
        item.status = ServiceAreaDraftStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaDraftStatus.IDLE) }
    }
}
