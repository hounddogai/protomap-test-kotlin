package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newSwapQuota("code-601", "title-780")
        assertEquals(SwapQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newSwapQuota("code-601", "title-780")
        try {
            item.moveTo(SwapQuotaStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapQuotaStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSwapQuota("code-601", "title-780")
        item.status = SwapQuotaStatus.FAILED
        assertFails("move") { item.moveTo(SwapQuotaStatus.NEW) }
    }
}
