package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newBackhaulMapping("code-32", "label-732")
        assertEquals(BackhaulMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBackhaulMapping("code-32", "label-732")
        try {
            item.moveTo(BackhaulMappingStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulMappingStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBackhaulMapping("code-32", "label-732")
        item.status = BackhaulMappingStatus.STOPPED
        assertFails("move") { item.moveTo(BackhaulMappingStatus.IDLE) }
    }
}
