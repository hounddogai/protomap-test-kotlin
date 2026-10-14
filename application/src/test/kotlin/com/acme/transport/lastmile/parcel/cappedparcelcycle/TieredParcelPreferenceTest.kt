package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredParcelPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newTieredParcelPreference("code-829", "title-514")
        assertEquals(TieredParcelPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTieredParcelPreference("code-829", "title-514")
        try {
            item.moveTo(TieredParcelPreferenceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredParcelPreferenceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredParcelPreference("code-829", "title-514")
        item.status = TieredParcelPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(TieredParcelPreferenceStatus.CREATED) }
    }
}
