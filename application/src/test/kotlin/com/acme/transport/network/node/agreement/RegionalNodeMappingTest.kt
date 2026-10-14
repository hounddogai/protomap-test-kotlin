package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNodeMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalNodeMapping("code-160", "name-371")
        assertEquals(RegionalNodeMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalNodeMapping("code-160", "name-371")
        try {
            item.moveTo(RegionalNodeMappingStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNodeMappingStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalNodeMapping("code-160", "name-371")
        item.status = RegionalNodeMappingStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalNodeMappingStatus.DRAFT) }
    }
}
