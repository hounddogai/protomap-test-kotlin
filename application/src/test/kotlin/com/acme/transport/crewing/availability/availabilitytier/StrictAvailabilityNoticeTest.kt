package com.acme.transport.crewing.availability.availabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAvailabilityNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newStrictAvailabilityNotice("code-949", "title-536")
        assertEquals(StrictAvailabilityNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newStrictAvailabilityNotice("code-949", "title-536")
        try {
            item.moveTo(StrictAvailabilityNoticeStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAvailabilityNoticeStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictAvailabilityNotice("code-949", "title-536")
        item.status = StrictAvailabilityNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(StrictAvailabilityNoticeStatus.CREATED) }
    }
}
