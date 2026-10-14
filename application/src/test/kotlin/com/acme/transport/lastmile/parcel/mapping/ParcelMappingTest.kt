package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelMappingTest {
    @Test
    fun `starts in created`() {
        val item = newParcelMapping("code-884", "title-992")
        assertEquals(ParcelMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newParcelMapping("code-884", "title-992")
        try {
            item.moveTo(ParcelMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelMapping("code-884", "title-992")
        item.status = ParcelMappingStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelMappingStatus.CREATED) }
    }
}
