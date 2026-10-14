package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCrossDockMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredCrossDockMapping("code-780", "name-817")
        assertEquals(TieredCrossDockMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredCrossDockMapping("code-780", "name-817")
        try {
            item.moveTo(TieredCrossDockMappingStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCrossDockMappingStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredCrossDockMapping("code-780", "name-817")
        item.status = TieredCrossDockMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredCrossDockMappingStatus.PLANNED) }
    }
}
