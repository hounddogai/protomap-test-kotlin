package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictHoldBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictHoldBundle("code-487", "name-166")
        assertEquals(StrictHoldBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictHoldBundle("code-487", "name-166")
        try {
            item.moveTo(StrictHoldBundleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictHoldBundleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictHoldBundle("code-487", "name-166")
        item.status = StrictHoldBundleStatus.STOPPED
        assertFails("move") { item.moveTo(StrictHoldBundleStatus.IDLE) }
    }
}
