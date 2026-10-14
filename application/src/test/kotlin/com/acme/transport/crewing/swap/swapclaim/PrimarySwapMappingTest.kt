package com.acme.transport.crewing.swap.swapclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySwapMappingTest {
    @Test
    fun `starts in open`() {
        val item = newPrimarySwapMapping("code-382", "title-295")
        assertEquals(PrimarySwapMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimarySwapMapping("code-382", "title-295")
        try {
            item.moveTo(PrimarySwapMappingStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySwapMappingStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimarySwapMapping("code-382", "title-295")
        item.status = PrimarySwapMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimarySwapMappingStatus.OPEN) }
    }
}
