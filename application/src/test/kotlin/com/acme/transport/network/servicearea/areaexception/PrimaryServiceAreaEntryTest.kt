package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryServiceAreaEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryServiceAreaEntry("code-962", "label-673")
        assertEquals(PrimaryServiceAreaEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPrimaryServiceAreaEntry("code-962", "label-673")
        try {
            item.moveTo(PrimaryServiceAreaEntryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryServiceAreaEntryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryServiceAreaEntry("code-962", "label-673")
        item.status = PrimaryServiceAreaEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryServiceAreaEntryStatus.PROPOSED) }
    }
}
