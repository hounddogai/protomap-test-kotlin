package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryMappingTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryMapping("code-907", "name-333")
        assertEquals(RedeliveryMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRedeliveryMapping("code-907", "name-333")
        try {
            item.moveTo(RedeliveryMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryMapping("code-907", "name-333")
        item.status = RedeliveryMappingStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryMappingStatus.NEW) }
    }
}
