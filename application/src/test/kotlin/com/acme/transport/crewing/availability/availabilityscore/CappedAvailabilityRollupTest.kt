package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAvailabilityRollupTest {
    @Test
    fun `starts in new`() {
        val item = newCappedAvailabilityRollup("code-850", "name-274")
        assertEquals(CappedAvailabilityRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCappedAvailabilityRollup("code-850", "name-274")
        try {
            item.moveTo(CappedAvailabilityRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAvailabilityRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedAvailabilityRollup("code-850", "name-274")
        item.status = CappedAvailabilityRollupStatus.FAILED
        assertFails("move") { item.moveTo(CappedAvailabilityRollupStatus.NEW) }
    }
}
