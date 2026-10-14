package com.acme.transport.crewing.swap.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newSwapRecord("code-731", "label-897")
        assertEquals(SwapRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSwapRecord("code-731", "label-897")
        try {
            item.moveTo(SwapRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSwapRecord("code-731", "label-897")
        item.status = SwapRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(SwapRecordStatus.REQUESTED) }
    }
}
