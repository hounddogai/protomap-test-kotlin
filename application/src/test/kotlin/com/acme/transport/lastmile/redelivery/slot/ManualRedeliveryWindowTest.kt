package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRedeliveryWindowTest {
    @Test
    fun `starts in created`() {
        val item = newManualRedeliveryWindow("code-916", "name-756")
        assertEquals(ManualRedeliveryWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualRedeliveryWindow("code-916", "name-756")
        try {
            item.moveTo(ManualRedeliveryWindowStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRedeliveryWindowStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualRedeliveryWindow("code-916", "name-756")
        item.status = ManualRedeliveryWindowStatus.RETIRED
        assertFails("move") { item.moveTo(ManualRedeliveryWindowStatus.CREATED) }
    }
}
