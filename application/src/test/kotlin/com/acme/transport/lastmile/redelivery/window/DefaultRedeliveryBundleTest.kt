package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRedeliveryBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        assertEquals(DefaultRedeliveryBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        try {
            item.moveTo(DefaultRedeliveryBundleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRedeliveryBundleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        item.status = DefaultRedeliveryBundleStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRedeliveryBundleStatus.IDLE) }
    }
}
