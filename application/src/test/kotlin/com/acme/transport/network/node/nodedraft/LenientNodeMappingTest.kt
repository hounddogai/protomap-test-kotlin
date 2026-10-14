package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNodeMappingTest {
    @Test
    fun `starts in created`() {
        val item = newLenientNodeMapping("code-111", "title-542")
        assertEquals(LenientNodeMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientNodeMapping("code-111", "title-542")
        try {
            item.moveTo(LenientNodeMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNodeMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientNodeMapping("code-111", "title-542")
        item.status = LenientNodeMappingStatus.RETIRED
        assertFails("move") { item.moveTo(LenientNodeMappingStatus.CREATED) }
    }
}
