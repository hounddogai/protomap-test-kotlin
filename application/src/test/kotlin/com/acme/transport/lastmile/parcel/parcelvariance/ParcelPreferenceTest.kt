package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newParcelPreference("code-249", "label-796")
        assertEquals(ParcelPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newParcelPreference("code-249", "label-796")
        try {
            item.moveTo(ParcelPreferenceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelPreferenceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newParcelPreference("code-249", "label-796")
        item.status = ParcelPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ParcelPreferenceStatus.OPEN) }
    }
}
