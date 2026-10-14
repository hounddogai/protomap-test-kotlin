package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSwapOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalSwapOverride("code-345", "label-967")
        assertEquals(GlobalSwapOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalSwapOverride("code-345", "label-967")
        try {
            item.moveTo(GlobalSwapOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSwapOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalSwapOverride("code-345", "label-967")
        item.status = GlobalSwapOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalSwapOverrideStatus.CREATED) }
    }
}
