package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newSwapMapping("code-188", "label-923")
        assertEquals(SwapMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSwapMapping("code-188", "label-923")
        try {
            item.moveTo(SwapMappingStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapMappingStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSwapMapping("code-188", "label-923")
        item.status = SwapMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(SwapMappingStatus.REQUESTED) }
    }
}
