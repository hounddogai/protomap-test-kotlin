package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelMappingTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalParcelMapping("code-180", "name-69")
        assertEquals(RegionalParcelMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRegionalParcelMapping("code-180", "name-69")
        try {
            item.moveTo(RegionalParcelMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalParcelMapping("code-180", "name-69")
        item.status = RegionalParcelMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalParcelMappingStatus.OPEN) }
    }
}
