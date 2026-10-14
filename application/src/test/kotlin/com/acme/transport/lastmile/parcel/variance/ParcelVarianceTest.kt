package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newParcelVariance("code-237", "name-496")
        assertEquals(ParcelVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newParcelVariance("code-237", "name-496")
        try {
            item.moveTo(ParcelVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newParcelVariance("code-237", "name-496")
        item.status = ParcelVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ParcelVarianceStatus.PROPOSED) }
    }
}
