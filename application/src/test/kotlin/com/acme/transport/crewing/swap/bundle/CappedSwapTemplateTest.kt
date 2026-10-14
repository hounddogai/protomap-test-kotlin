package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedSwapTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedSwapTemplate("code-498", "name-345")
        assertEquals(CappedSwapTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCappedSwapTemplate("code-498", "name-345")
        try {
            item.moveTo(CappedSwapTemplateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedSwapTemplateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedSwapTemplate("code-498", "name-345")
        item.status = CappedSwapTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedSwapTemplateStatus.PLANNED) }
    }
}
