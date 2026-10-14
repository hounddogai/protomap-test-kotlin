package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newSwapManifest("code-775", "title-240")
        assertEquals(SwapManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSwapManifest("code-775", "title-240")
        try {
            item.moveTo(SwapManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSwapManifest("code-775", "title-240")
        item.status = SwapManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(SwapManifestStatus.REQUESTED) }
    }
}
