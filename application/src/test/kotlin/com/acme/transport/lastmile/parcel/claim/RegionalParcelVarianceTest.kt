package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalParcelVariance("code-504", "name-863")
        assertEquals(RegionalParcelVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRegionalParcelVariance("code-504", "name-863")
        try {
            item.moveTo(RegionalParcelVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalParcelVariance("code-504", "name-863")
        item.status = RegionalParcelVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalParcelVarianceStatus.PLANNED) }
    }
}
