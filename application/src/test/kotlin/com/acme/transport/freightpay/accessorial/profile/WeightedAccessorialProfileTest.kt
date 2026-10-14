package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAccessorialProfileTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        assertEquals(WeightedAccessorialProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        try {
            item.moveTo(WeightedAccessorialProfileStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAccessorialProfileStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedAccessorialProfile("code-87", "name-964")
        item.status = WeightedAccessorialProfileStatus.FAILED
        assertFails("move") { item.moveTo(WeightedAccessorialProfileStatus.NEW) }
    }
}
