package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newSortationPolicy("code-941", "name-758")
        assertEquals(SortationPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSortationPolicy("code-941", "name-758")
        try {
            item.moveTo(SortationPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSortationPolicy("code-941", "name-758")
        item.status = SortationPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(SortationPolicyStatus.IDLE) }
    }
}
