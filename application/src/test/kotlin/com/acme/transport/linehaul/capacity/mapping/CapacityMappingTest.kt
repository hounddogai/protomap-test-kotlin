package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityMapping("code-478", "title-988")
        assertEquals(CapacityMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCapacityMapping("code-478", "title-988")
        try {
            item.moveTo(CapacityMappingStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityMappingStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityMapping("code-478", "title-988")
        item.status = CapacityMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityMappingStatus.PROPOSED) }
    }
}
