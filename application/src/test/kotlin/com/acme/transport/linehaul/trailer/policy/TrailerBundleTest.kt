package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerBundleTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerBundle("code-769", "name-397")
        assertEquals(TrailerBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTrailerBundle("code-769", "name-397")
        try {
            item.moveTo(TrailerBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerBundle("code-769", "name-397")
        item.status = TrailerBundleStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerBundleStatus.CREATED) }
    }
}
