package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCapacityMappingTest {
    @Test
    fun `starts in created`() {
        val item = newCappedCapacityMapping("code-311", "label-680")
        assertEquals(CappedCapacityMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedCapacityMapping("code-311", "label-680")
        try {
            item.moveTo(CappedCapacityMappingStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCapacityMappingStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedCapacityMapping("code-311", "label-680")
        item.status = CappedCapacityMappingStatus.RETIRED
        assertFails("move") { item.moveTo(CappedCapacityMappingStatus.CREATED) }
    }
}
