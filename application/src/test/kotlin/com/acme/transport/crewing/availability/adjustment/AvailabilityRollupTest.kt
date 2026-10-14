package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityRollupTest {
    @Test
    fun `starts in open`() {
        val item = newAvailabilityRollup("code-182", "label-516")
        assertEquals(AvailabilityRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAvailabilityRollup("code-182", "label-516")
        try {
            item.moveTo(AvailabilityRollupStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityRollupStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAvailabilityRollup("code-182", "label-516")
        item.status = AvailabilityRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(AvailabilityRollupStatus.OPEN) }
    }
}
