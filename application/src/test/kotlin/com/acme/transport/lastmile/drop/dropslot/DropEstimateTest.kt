package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newDropEstimate("code-998", "label-222")
        assertEquals(DropEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropEstimate("code-998", "label-222")
        try {
            item.moveTo(DropEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropEstimate("code-998", "label-222")
        item.status = DropEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(DropEstimateStatus.REQUESTED) }
    }
}
