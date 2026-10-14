package com.acme.transport.emissions.routeoffset.offsetmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackRouteOffsetMapping("code-381", "label-337")
        assertEquals(FallbackRouteOffsetMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFallbackRouteOffsetMapping("code-381", "label-337")
        try {
            item.moveTo(FallbackRouteOffsetMappingStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetMappingStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackRouteOffsetMapping("code-381", "label-337")
        item.status = FallbackRouteOffsetMappingStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackRouteOffsetMappingStatus.DRAFT) }
    }
}
