package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalCapacityMapping("code-185", "label-354")
        assertEquals(RegionalCapacityMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalCapacityMapping("code-185", "label-354")
        try {
            item.moveTo(RegionalCapacityMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalCapacityMapping("code-185", "label-354")
        item.status = RegionalCapacityMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalCapacityMappingStatus.PROPOSED) }
    }
}
