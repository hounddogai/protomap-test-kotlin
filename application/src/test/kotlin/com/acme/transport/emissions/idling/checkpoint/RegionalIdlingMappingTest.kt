package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIdlingMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalIdlingMapping("code-341", "title-107")
        assertEquals(RegionalIdlingMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalIdlingMapping("code-341", "title-107")
        try {
            item.moveTo(RegionalIdlingMappingStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIdlingMappingStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalIdlingMapping("code-341", "title-107")
        item.status = RegionalIdlingMappingStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalIdlingMappingStatus.IDLE) }
    }
}
