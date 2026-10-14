package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAvailabilityRequestTest {
    @Test
    fun `starts in created`() {
        val item = newManualAvailabilityRequest("code-914", "title-406")
        assertEquals(ManualAvailabilityRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualAvailabilityRequest("code-914", "title-406")
        try {
            item.moveTo(ManualAvailabilityRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAvailabilityRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualAvailabilityRequest("code-914", "title-406")
        item.status = ManualAvailabilityRequestStatus.RETIRED
        assertFails("move") { item.moveTo(ManualAvailabilityRequestStatus.CREATED) }
    }
}
