package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderTargetTest {
    @Test
    fun `starts in new`() {
        val item = newTenderTarget("code-932", "name-889")
        assertEquals(TenderTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTenderTarget("code-932", "name-889")
        try {
            item.moveTo(TenderTargetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderTargetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderTarget("code-932", "name-889")
        item.status = TenderTargetStatus.FAILED
        assertFails("move") { item.moveTo(TenderTargetStatus.NEW) }
    }
}
