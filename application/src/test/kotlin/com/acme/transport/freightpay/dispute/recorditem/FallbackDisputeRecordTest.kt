package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDisputeRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackDisputeRecord("code-221", "name-19")
        assertEquals(FallbackDisputeRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFallbackDisputeRecord("code-221", "name-19")
        try {
            item.moveTo(FallbackDisputeRecordStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDisputeRecordStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackDisputeRecord("code-221", "name-19")
        item.status = FallbackDisputeRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackDisputeRecordStatus.PROPOSED) }
    }
}
