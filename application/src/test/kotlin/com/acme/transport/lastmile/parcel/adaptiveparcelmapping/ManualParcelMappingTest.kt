package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualParcelMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualParcelMapping("code-143", "name-543")
        assertEquals(ManualParcelMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualParcelMapping("code-143", "name-543")
        try {
            item.moveTo(ManualParcelMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualParcelMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualParcelMapping("code-143", "name-543")
        item.status = ManualParcelMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualParcelMappingStatus.PROPOSED) }
    }
}
