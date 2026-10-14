package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedEvChargeRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedEvChargeRecord("code-573", "label-633")
        assertEquals(CappedEvChargeRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCappedEvChargeRecord("code-573", "label-633")
        try {
            item.moveTo(CappedEvChargeRecordStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedEvChargeRecordStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedEvChargeRecord("code-573", "label-633")
        item.status = CappedEvChargeRecordStatus.CLOSED
        assertFails("move") { item.moveTo(CappedEvChargeRecordStatus.DRAFT) }
    }
}
