package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientZoneNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newLenientZoneNotice("code-648", "name-563")
        assertEquals(LenientZoneNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientZoneNotice("code-648", "name-563")
        try {
            item.moveTo(LenientZoneNoticeStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientZoneNoticeStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientZoneNotice("code-648", "name-563")
        item.status = LenientZoneNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(LenientZoneNoticeStatus.CREATED) }
    }
}
