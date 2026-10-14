package com.acme.transport.emissions.evcharge.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeBundleTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeBundle("code-107", "name-611")
        assertEquals(EvChargeBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEvChargeBundle("code-107", "name-611")
        try {
            item.moveTo(EvChargeBundleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeBundleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeBundle("code-107", "name-611")
        item.status = EvChargeBundleStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeBundleStatus.NEW) }
    }
}
