package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTireGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryTireGroup("code-452", "name-166")
        assertEquals(PrimaryTireGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryTireGroup("code-452", "name-166")
        try {
            item.moveTo(PrimaryTireGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTireGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryTireGroup("code-452", "name-166")
        item.status = PrimaryTireGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryTireGroupStatus.PROPOSED) }
    }
}
