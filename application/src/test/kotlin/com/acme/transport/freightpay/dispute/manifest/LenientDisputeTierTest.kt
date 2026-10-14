package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputeTierTest {
    @Test
    fun `starts in new`() {
        val item = newLenientDisputeTier("code-546", "title-404")
        assertEquals(LenientDisputeTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLenientDisputeTier("code-546", "title-404")
        try {
            item.moveTo(LenientDisputeTierStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputeTierStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientDisputeTier("code-546", "title-404")
        item.status = LenientDisputeTierStatus.FAILED
        assertFails("move") { item.moveTo(LenientDisputeTierStatus.NEW) }
    }
}
