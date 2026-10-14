package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerYardRecordTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalTrailerYardRecord("code-557", "title-335")
        assertEquals(RegionalTrailerYardRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRegionalTrailerYardRecord("code-557", "title-335")
        try {
            item.moveTo(RegionalTrailerYardRecordStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerYardRecordStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalTrailerYardRecord("code-557", "title-335")
        item.status = RegionalTrailerYardRecordStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalTrailerYardRecordStatus.CREATED) }
    }
}
