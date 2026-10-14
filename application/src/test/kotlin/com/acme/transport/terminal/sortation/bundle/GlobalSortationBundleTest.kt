package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalSortationBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalSortationBundle("code-289", "title-808")
        assertEquals(GlobalSortationBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalSortationBundle("code-289", "title-808")
        try {
            item.moveTo(GlobalSortationBundleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalSortationBundleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalSortationBundle("code-289", "title-808")
        item.status = GlobalSortationBundleStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalSortationBundleStatus.IDLE) }
    }
}
