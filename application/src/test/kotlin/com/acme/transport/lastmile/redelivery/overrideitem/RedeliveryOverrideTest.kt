package com.acme.transport.lastmile.redelivery.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryOverride("code-748", "label-707")
        assertEquals(RedeliveryOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRedeliveryOverride("code-748", "label-707")
        try {
            item.moveTo(RedeliveryOverrideStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryOverrideStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryOverride("code-748", "label-707")
        item.status = RedeliveryOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryOverrideStatus.IDLE) }
    }
}
