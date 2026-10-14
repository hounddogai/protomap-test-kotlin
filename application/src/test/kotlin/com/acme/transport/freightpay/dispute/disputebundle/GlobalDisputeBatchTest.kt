package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDisputeBatchTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalDisputeBatch("code-71", "title-155")
        assertEquals(GlobalDisputeBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalDisputeBatch("code-71", "title-155")
        try {
            item.moveTo(GlobalDisputeBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDisputeBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalDisputeBatch("code-71", "title-155")
        item.status = GlobalDisputeBatchStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalDisputeBatchStatus.CREATED) }
    }
}
