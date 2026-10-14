package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRouteOffsetExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRouteOffsetException("code-944", "label-519")
        assertEquals(LenientRouteOffsetExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientRouteOffsetException("code-944", "label-519")
        try {
            item.moveTo(LenientRouteOffsetExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRouteOffsetExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRouteOffsetException("code-944", "label-519")
        item.status = LenientRouteOffsetExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRouteOffsetExceptionStatus.CREATED) }
    }
}
