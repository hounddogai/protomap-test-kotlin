package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyParcelMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyParcelMapping("code-386", "label-530")
        assertEquals(LegacyParcelMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyParcelMapping("code-386", "label-530")
        try {
            item.moveTo(LegacyParcelMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyParcelMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyParcelMapping("code-386", "label-530")
        item.status = LegacyParcelMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyParcelMappingStatus.REQUESTED) }
    }
}
