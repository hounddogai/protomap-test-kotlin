package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeRecord("code-388", "label-694")
        assertEquals(EvChargeRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newEvChargeRecord("code-388", "label-694")
        try {
            item.moveTo(EvChargeRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeRecord("code-388", "label-694")
        item.status = EvChargeRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeRecordStatus.PROPOSED) }
    }
}
