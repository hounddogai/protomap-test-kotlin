package com.acme.transport.linehaul.trailer.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTrailerBundleTest {
    @Test
    fun `starts in open`() {
        val item = newLenientTrailerBundle("code-218", "title-833")
        assertEquals(LenientTrailerBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLenientTrailerBundle("code-218", "title-833")
        try {
            item.moveTo(LenientTrailerBundleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTrailerBundleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientTrailerBundle("code-218", "title-833")
        item.status = LenientTrailerBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientTrailerBundleStatus.OPEN) }
    }
}
