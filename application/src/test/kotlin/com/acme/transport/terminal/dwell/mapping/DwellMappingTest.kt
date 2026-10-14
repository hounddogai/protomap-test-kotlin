package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellMappingTest {
    @Test
    fun `starts in new`() {
        val item = newDwellMapping("code-384", "title-597")
        assertEquals(DwellMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDwellMapping("code-384", "title-597")
        try {
            item.moveTo(DwellMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellMapping("code-384", "title-597")
        item.status = DwellMappingStatus.FAILED
        assertFails("move") { item.moveTo(DwellMappingStatus.NEW) }
    }
}
