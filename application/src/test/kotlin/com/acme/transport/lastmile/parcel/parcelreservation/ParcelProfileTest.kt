package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelProfileTest {
    @Test
    fun `starts in created`() {
        val item = newParcelProfile("code-179", "name-263")
        assertEquals(ParcelProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newParcelProfile("code-179", "name-263")
        try {
            item.moveTo(ParcelProfileStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelProfileStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelProfile("code-179", "name-263")
        item.status = ParcelProfileStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelProfileStatus.CREATED) }
    }
}
